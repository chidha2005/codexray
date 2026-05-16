export interface AnalysisSummary {
  classCount: number;
  methodCount: number;
  fieldCount: number;
  localVariableCount: number;
  parameterCount: number;
  objectCreationCount: number;
  loopCount: number;
  conditionCount: number;
  methodCallCount: number;
  riskFindingCount: number;
}

export interface ClassModel {
  name: string;
  type: string;
  packageName: string;
  modifiers: string[];
  extendedTypes: string[];
  implementedTypes: string[];
  beginLine: number;
  endLine: number;
}

export interface MethodModel {
  name: string;
  returnType: string;
  modifiers: string[];
  parameterCount: number;
  beginLine: number;
  endLine: number;
  recursive: boolean;
  staticMethod: boolean;
}

export interface FieldModel {
  name: string;
  type: string;
  modifiers: string[];
  primitive: boolean;
  collectionLike: boolean;
  staticField: boolean;
  line: number;
}

export interface VariableModel {
  name: string;
  type: string;
  scope: string;
  primitive: boolean;
  collectionLike: boolean;
  objectReference: boolean;
  line: number;
}

export interface ObjectCreationModel {
  type: string;
  expression: string;
  allocationCategory: string;
  line: number;
  memoryImpact: string;
}

export interface LoopModel {
  loopType: string;
  beginLine: number;
  endLine: number;
  riskLevel: string;
  reason: string;
}

export interface ConditionModel {
  conditionType: string;
  expression: string;
  line: number;
}

export interface MethodCallModel {
  methodName: string;
  scope: string;
  line: number;
  category: string;
}

export interface RiskFinding {
  severity: string;
  category: string;
  title: string;
  description: string;
  recommendation: string;
  line: number;
}

export interface JavaAnalyzeResponse {
  analysisId: string;
  language: string;
  summary: AnalysisSummary;
  classes: ClassModel[];
  methods: MethodModel[];
  fields: FieldModel[];
  variables: VariableModel[];
  objectCreations: ObjectCreationModel[];
  loops: LoopModel[];
  conditions: ConditionModel[];
  methodCalls: MethodCallModel[];
  riskFindings: RiskFinding[];
}
