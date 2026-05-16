package com.codexray.analyzer;

import com.codexray.dto.JavaAnalyzeResponse;
import com.codexray.exception.CodeAnalysisException;
import com.codexray.model.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JavaStaticAnalyzer {

    public JavaAnalyzeResponse analyze(String sourceCode) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);

            List<ClassModel> classes = extractClasses(cu);
            List<MethodModel> methods = extractMethods(cu);
            List<FieldModel> fields = extractFields(cu);
            List<VariableModel> variables = extractVariables(cu);
            List<ObjectCreationModel> objectCreations = extractObjectCreations(cu);
            List<LoopModel> loops = extractLoops(cu);
            List<ConditionModel> conditions = extractConditions(cu);
            List<MethodCallModel> methodCalls = extractMethodCalls(cu);
            List<RiskFinding> riskFindings = detectRisks(cu, fields, variables, methodCalls);

            AnalysisSummary summary = new AnalysisSummary(
                    classes.size(),
                    methods.size(),
                    fields.size(),
                    (int) variables.stream().filter(v -> "LOCAL".equals(v.scope())).count(),
                    (int) variables.stream().filter(v -> "PARAMETER".equals(v.scope())).count(),
                    objectCreations.size(),
                    loops.size(),
                    conditions.size(),
                    methodCalls.size(),
                    riskFindings.size()
            );

            return new JavaAnalyzeResponse(
                    UUID.randomUUID(),
                    "JAVA",
                    summary,
                    classes,
                    methods,
                    fields,
                    variables,
                    objectCreations,
                    loops,
                    conditions,
                    methodCalls,
                    riskFindings
            );
        } catch (Exception ex) {
            throw new CodeAnalysisException("Unable to parse/analyze Java source code", ex);
        }
    }

    private List<ClassModel> extractClasses(CompilationUnit cu) {
        String packageName = cu.getPackageDeclaration().map(pd -> pd.getName().asString()).orElse("");
        List<ClassModel> result = new ArrayList<>();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> result.add(new ClassModel(
                clazz.getNameAsString(),
                clazz.isInterface() ? "INTERFACE" : "CLASS",
                packageName,
                clazz.getModifiers().stream().map(Object::toString).toList(),
                clazz.getExtendedTypes().stream().map(Object::toString).toList(),
                clazz.getImplementedTypes().stream().map(Object::toString).toList(),
                beginLine(clazz),
                endLine(clazz)
        )));

        cu.findAll(EnumDeclaration.class).forEach(enumDecl -> result.add(new ClassModel(
                enumDecl.getNameAsString(),
                "ENUM",
                packageName,
                enumDecl.getModifiers().stream().map(Object::toString).toList(),
                List.of(),
                List.of(),
                beginLine(enumDecl),
                endLine(enumDecl)
        )));

        return result;
    }

    private List<MethodModel> extractMethods(CompilationUnit cu) {
        return cu.findAll(MethodDeclaration.class).stream()
                .map(method -> new MethodModel(
                        method.getNameAsString(),
                        method.getType().asString(),
                        method.getModifiers().stream().map(Object::toString).toList(),
                        method.getParameters().size(),
                        beginLine(method),
                        endLine(method),
                        isRecursive(method),
                        method.isStatic()
                ))
                .toList();
    }

    private List<FieldModel> extractFields(CompilationUnit cu) {
        List<FieldModel> fields = new ArrayList<>();
        cu.findAll(FieldDeclaration.class).forEach(fieldDeclaration -> fieldDeclaration.getVariables().forEach(variable -> {
            String type = variable.getType().asString();
            fields.add(new FieldModel(
                    variable.getNameAsString(),
                    type,
                    fieldDeclaration.getModifiers().stream().map(Object::toString).toList(),
                    isPrimitiveType(type),
                    isCollectionLike(type),
                    fieldDeclaration.isStatic(),
                    beginLine(variable)
            ));
        }));
        return fields;
    }

    private List<VariableModel> extractVariables(CompilationUnit cu) {
        List<VariableModel> variables = new ArrayList<>();

        cu.findAll(Parameter.class).forEach(parameter -> {
            String type = parameter.getType().asString();
            variables.add(new VariableModel(
                    parameter.getNameAsString(), type, "PARAMETER", isPrimitiveType(type),
                    isCollectionLike(type), !isPrimitiveType(type), beginLine(parameter)
            ));
        });

        cu.findAll(VariableDeclarator.class).forEach(variable -> {
            if (variable.getParentNode().filter(FieldDeclaration.class::isInstance).isPresent()) {
                return;
            }
            String type = variable.getType().asString();
            variables.add(new VariableModel(
                    variable.getNameAsString(), type, "LOCAL", isPrimitiveType(type),
                    isCollectionLike(type), !isPrimitiveType(type), beginLine(variable)
            ));
        });
        return variables;
    }

    private List<ObjectCreationModel> extractObjectCreations(CompilationUnit cu) {
        return cu.findAll(ObjectCreationExpr.class).stream()
                .map(expr -> {
                    String type = expr.getType().asString();
                    return new ObjectCreationModel(
                            type,
                            expr.toString(),
                            allocationCategory(type),
                            beginLine(expr),
                            memoryImpact(type)
                    );
                })
                .toList();
    }

    private List<LoopModel> extractLoops(CompilationUnit cu) {
        List<LoopModel> loops = new ArrayList<>();
        cu.findAll(ForStmt.class).forEach(loop -> loops.add(new LoopModel("FOR", beginLine(loop), endLine(loop), "MEDIUM", "Loop can multiply object allocations depending on input size.")));
        cu.findAll(ForEachStmt.class).forEach(loop -> loops.add(new LoopModel("FOR_EACH", beginLine(loop), endLine(loop), "MEDIUM", "Enhanced loop can process unbounded collections if input size is not controlled.")));
        cu.findAll(WhileStmt.class).forEach(loop -> loops.add(new LoopModel("WHILE", beginLine(loop), endLine(loop), "HIGH", "While loop may be unbounded if termination condition is not guaranteed.")));
        cu.findAll(DoStmt.class).forEach(loop -> loops.add(new LoopModel("DO_WHILE", beginLine(loop), endLine(loop), "HIGH", "Do-while loop executes at least once and can be risky if unbounded.")));
        return loops;
    }

    private List<ConditionModel> extractConditions(CompilationUnit cu) {
        List<ConditionModel> conditions = new ArrayList<>();
        cu.findAll(IfStmt.class).forEach(stmt -> conditions.add(new ConditionModel("IF", stmt.getCondition().toString(), beginLine(stmt))));
        cu.findAll(SwitchStmt.class).forEach(stmt -> conditions.add(new ConditionModel("SWITCH", stmt.getSelector().toString(), beginLine(stmt))));
        return conditions;
    }

    private List<MethodCallModel> extractMethodCalls(CompilationUnit cu) {
        return cu.findAll(MethodCallExpr.class).stream()
                .map(call -> new MethodCallModel(
                        call.getNameAsString(),
                        call.getScope().map(Object::toString).orElse(""),
                        beginLine(call),
                        categorizeMethodCall(call)
                ))
                .toList();
    }

    private List<RiskFinding> detectRisks(CompilationUnit cu, List<FieldModel> fields, List<VariableModel> variables, List<MethodCallModel> methodCalls) {
        List<RiskFinding> findings = new ArrayList<>();
        detectFindAllRisk(methodCalls, findings);
        detectReadAllBytesRisk(methodCalls, findings);
        detectCollectionRisk(fields, variables, findings);
        detectObjectCreationInsideLoop(cu, findings);
        detectStringConcatInsideLoop(cu, findings);
        detectStaticMutableCollection(fields, findings);
        detectRecursiveMethods(cu, findings);
        detectParallelStreamRisk(methodCalls, findings);
        return findings;
    }

    private void detectFindAllRisk(List<MethodCallModel> methodCalls, List<RiskFinding> findings) {
        methodCalls.stream().filter(call -> "findAll".equals(call.methodName())).forEach(call -> findings.add(new RiskFinding(
                "HIGH", "DATABASE_MEMORY", "Potential unbounded database load",
                "findAll() may load a full table into memory.",
                "Use pagination, streaming query, or bounded batch processing.", call.line())));
    }

    private void detectReadAllBytesRisk(List<MethodCallModel> methodCalls, List<RiskFinding> findings) {
        methodCalls.stream().filter(call -> "readAllBytes".equals(call.methodName()) || "readString".equals(call.methodName())).forEach(call -> findings.add(new RiskFinding(
                "CRITICAL", "FILE_MEMORY", "Potential full file load into heap",
                "Reading the full file into memory can cause heap pressure or OutOfMemoryError for large files.",
                "Use Files.lines(), BufferedReader, or streaming processing.", call.line())));
    }

    private void detectCollectionRisk(List<FieldModel> fields, List<VariableModel> variables, List<RiskFinding> findings) {
        variables.stream().filter(VariableModel::collectionLike).forEach(v -> findings.add(new RiskFinding(
                "MEDIUM", "COLLECTION_MEMORY", "Collection memory growth risk",
                "Collection variable '" + v.name() + "' can grow based on input size.",
                "Apply size limits, pagination, batching, or streaming where applicable.", v.line())));

        fields.stream().filter(FieldModel::collectionLike).forEach(f -> findings.add(new RiskFinding(
                f.staticField() ? "HIGH" : "MEDIUM", "FIELD_COLLECTION_MEMORY", "Collection field memory retention risk",
                "Collection field '" + f.name() + "' can retain objects beyond method execution.",
                "Use bounded collections, cache eviction, or avoid mutable state.", f.line())));
    }

    private void detectObjectCreationInsideLoop(CompilationUnit cu, List<RiskFinding> findings) {
        cu.findAll(ObjectCreationExpr.class).forEach(expr -> {
            if (isInsideLoop(expr)) {
                findings.add(new RiskFinding(
                        "MEDIUM", "ALLOCATION_IN_LOOP", "Object allocation inside loop",
                        "Object '" + expr.getType().asString() + "' is created inside a loop.",
                        "Consider pre-sizing collections or moving allocation outside the loop where safe.", beginLine(expr)));
            }
        });
    }

    private void detectStringConcatInsideLoop(CompilationUnit cu, List<RiskFinding> findings) {
        cu.findAll(BinaryExpr.class).forEach(expr -> {
            if (expr.getOperator() == BinaryExpr.Operator.PLUS && isInsideLoop(expr)) {
                findings.add(new RiskFinding(
                        "MEDIUM", "STRING_ALLOCATION", "Possible string concatenation inside loop",
                        "String concatenation inside loops can create many temporary String objects.",
                        "Use StringBuilder with pre-sized capacity for repeated concatenation.", beginLine(expr)));
            }
        });
    }

    private void detectStaticMutableCollection(List<FieldModel> fields, List<RiskFinding> findings) {
        fields.stream().filter(FieldModel::staticField).filter(FieldModel::collectionLike).forEach(field -> findings.add(new RiskFinding(
                "HIGH", "STATIC_COLLECTION", "Static mutable collection risk",
                "Static collection '" + field.name() + "' can retain memory for the lifetime of the classloader.",
                "Use bounded cache such as Caffeine with maximumSize and expiration.", field.line())));
    }

    private void detectRecursiveMethods(CompilationUnit cu, List<RiskFinding> findings) {
        cu.findAll(MethodDeclaration.class).stream().filter(this::isRecursive).forEach(method -> findings.add(new RiskFinding(
                "HIGH", "STACK_OVERFLOW", "Recursive method detected",
                "Method '" + method.getNameAsString() + "' appears to call itself.",
                "Add max-depth checks or convert deep recursion to iterative logic using a stack/queue.", beginLine(method))));
    }

    private void detectParallelStreamRisk(List<MethodCallModel> methodCalls, List<RiskFinding> findings) {
        methodCalls.stream().filter(call -> "parallelStream".equals(call.methodName())).forEach(call -> findings.add(new RiskFinding(
                "MEDIUM", "PARALLELISM", "parallelStream detected",
                "parallelStream can increase CPU and memory pressure when used on large collections or request threads.",
                "Use controlled executors or benchmark before using parallel stream in server-side code.", call.line())));
    }

    private boolean isRecursive(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        return method.findAll(MethodCallExpr.class).stream().anyMatch(call -> methodName.equals(call.getNameAsString()));
    }

    private boolean isInsideLoop(Node node) {
        Optional<Node> current = node.getParentNode();
        while (current.isPresent()) {
            Node parent = current.get();
            if (parent instanceof ForStmt || parent instanceof ForEachStmt || parent instanceof WhileStmt || parent instanceof DoStmt) {
                return true;
            }
            current = parent.getParentNode();
        }
        return false;
    }

    private String categorizeMethodCall(MethodCallExpr call) {
        String name = call.getNameAsString();
        if (Set.of("findAll", "saveAll", "deleteAll").contains(name)) return "DATABASE";
        if (Set.of("readAllBytes", "readString", "write").contains(name)) return "FILE_IO";
        if (Set.of("add", "put", "addAll", "putAll").contains(name)) return "COLLECTION_MUTATION";
        if (Set.of("stream", "parallelStream", "collect").contains(name)) return "STREAM";
        return "GENERAL";
    }

    private String allocationCategory(String type) {
        if (isCollectionLike(type)) return "COLLECTION";
        if ("StringBuilder".equals(type) || "StringBuffer".equals(type)) return "STRING_BUFFER";
        if (type.endsWith("[]")) return "ARRAY";
        return "OBJECT";
    }

    private String memoryImpact(String type) {
        if (isCollectionLike(type)) return "Collection allocation may grow based on input size and retained elements.";
        if ("HashMap".equals(type)) return "HashMap has object overhead, internal table overhead, and node overhead per entry.";
        if ("ArrayList".equals(type)) return "ArrayList has object overhead and internal Object[] array overhead.";
        if ("StringBuilder".equals(type)) return "StringBuilder allocates an internal buffer that grows with appended content.";
        return "Standard heap object allocation.";
    }

    private boolean isPrimitiveType(String type) {
        return Set.of("byte", "short", "int", "long", "float", "double", "boolean", "char").contains(type);
    }

    private boolean isCollectionLike(String type) {
        String normalized = type.replaceAll("\\s+", "");
        return normalized.contains("List") || normalized.contains("ArrayList") || normalized.contains("LinkedList")
                || normalized.contains("Set") || normalized.contains("HashSet") || normalized.contains("Map")
                || normalized.contains("HashMap") || normalized.contains("ConcurrentHashMap") || normalized.contains("Queue")
                || normalized.contains("Deque") || normalized.contains("Collection");
    }

    private int beginLine(Node node) {
        return node.getBegin().map(position -> position.line).orElse(-1);
    }

    private int endLine(Node node) {
        return node.getEnd().map(position -> position.line).orElse(-1);
    }
}
