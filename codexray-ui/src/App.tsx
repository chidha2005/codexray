import { useState } from 'react';
import Editor from '@monaco-editor/react';
import {
  Alert,
  AppBar,
  Box,
  Button,
  Chip,
  CircularProgress,
  Container,
  Grid,
  Paper,
  Stack,
  Tab,
  Tabs,
  Toolbar,
  Typography
} from '@mui/material';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import BugReportIcon from '@mui/icons-material/BugReport';
import { analyzeJava } from './api/analyzerApi';
import { JavaAnalyzeResponse } from './types';
import SectionCard from './components/SectionCard';
import SummaryGrid from './components/SummaryGrid';
import RiskPanel from './components/RiskPanel';
import DataTable from './components/DataTable';

const defaultCode = `import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class DemoAnalyzerTarget {
    private static Map<String, Object> cache = new HashMap<>();

    public List<String> process(List<String> input) throws Exception {
        List<String> result = new ArrayList<>();

        for (String value : input) {
            String normalized = value.trim().toLowerCase();
            result.add(normalized);
        }

        byte[] fileData = Files.readAllBytes(Path.of("large-file.csv"));
        return result;
    }

    public int recursive(int value) {
        if (value <= 0) {
            return 0;
        }
        return recursive(value - 1);
    }
}`;

export default function App() {
  const [code, setCode] = useState(defaultCode);
  const [result, setResult] = useState<JavaAnalyzeResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState(0);

  async function runAnalysis() {
    setLoading(true);
    setError(null);
    try {
      const data = await analyzeJava(code);
      setResult(data);
      setTab(0);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Analysis failed';
      setError(message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box minHeight="100vh">
      <AppBar position="sticky" color="transparent" elevation={0} sx={{ backdropFilter: 'blur(10px)', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
        <Toolbar>
          <BugReportIcon sx={{ mr: 1, color: 'primary.main' }} />
          <Typography variant="h6" fontWeight={900}>CodeXRay MVP</Typography>
          <Chip label="Java Static Analyzer" size="small" sx={{ ml: 2 }} />
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ py: 3 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={5}>
            <Paper sx={{ p: 2, height: 'calc(100vh - 130px)', display: 'flex', flexDirection: 'column' }} variant="outlined">
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                <Box>
                  <Typography variant="h6" fontWeight={800}>Paste Java Code</Typography>
                  <Typography variant="body2" color="text.secondary">MVP-1 performs static anatomy and risk analysis.</Typography>
                </Box>
                <Button variant="contained" startIcon={loading ? <CircularProgress size={16} /> : <PlayArrowIcon />} onClick={runAnalysis} disabled={loading}>
                  Analyze
                </Button>
              </Stack>

              <Box sx={{ flex: 1, border: '1px solid rgba(255,255,255,0.1)', borderRadius: 2, overflow: 'hidden' }}>
                <Editor
                  language="java"
                  theme="vs-dark"
                  value={code}
                  onChange={(value) => setCode(value ?? '')}
                  options={{ minimap: { enabled: false }, fontSize: 14, wordWrap: 'on', automaticLayout: true }}
                />
              </Box>
            </Paper>
          </Grid>

          <Grid item xs={12} md={7}>
            <Paper sx={{ p: 2, minHeight: 'calc(100vh - 130px)' }} variant="outlined">
              <Typography variant="h5" fontWeight={900}>Analysis Output</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                View the extracted code anatomy, memory-risk indicators, and analyzer JSON.
              </Typography>

              {error && <Alert severity="error" sx={{ mb: 2 }}>{error}. Check that the backend is running on http://localhost:8080.</Alert>}

              {!result && !loading && (
                <Alert severity="info">Click Analyze to see CodeXRay output for the sample code.</Alert>
              )}

              {loading && <Alert severity="info">Analyzing Java source...</Alert>}

              {result && (
                <>
                  <Tabs value={tab} onChange={(_, value) => setTab(value)} variant="scrollable" scrollButtons="auto" sx={{ mb: 2 }}>
                    <Tab label="Summary" />
                    <Tab label="Risks" />
                    <Tab label="Anatomy" />
                    <Tab label="Raw JSON" />
                  </Tabs>

                  {tab === 0 && (
                    <Stack spacing={2}>
                      <Alert severity="success">Analysis ID: {result.analysisId}</Alert>
                      <SummaryGrid summary={result.summary} />
                    </Stack>
                  )}

                  {tab === 1 && <RiskPanel risks={result.riskFindings} />}

                  {tab === 2 && (
                    <>
                      <SectionCard title="Classes">
                        <DataTable rows={result.classes as unknown as Record<string, unknown>[]} columns={[
                          { key: 'name', label: 'Name' },
                          { key: 'type', label: 'Type' },
                          { key: 'packageName', label: 'Package' },
                          { key: 'beginLine', label: 'Start' },
                          { key: 'endLine', label: 'End' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Methods">
                        <DataTable rows={result.methods as unknown as Record<string, unknown>[]} columns={[
                          { key: 'name', label: 'Name' },
                          { key: 'returnType', label: 'Return' },
                          { key: 'parameterCount', label: 'Params' },
                          { key: 'recursive', label: 'Recursive' },
                          { key: 'beginLine', label: 'Start' },
                          { key: 'endLine', label: 'End' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Fields">
                        <DataTable rows={result.fields as unknown as Record<string, unknown>[]} columns={[
                          { key: 'name', label: 'Name' },
                          { key: 'type', label: 'Type' },
                          { key: 'staticField', label: 'Static' },
                          { key: 'collectionLike', label: 'Collection' },
                          { key: 'line', label: 'Line' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Variables">
                        <DataTable rows={result.variables as unknown as Record<string, unknown>[]} columns={[
                          { key: 'name', label: 'Name' },
                          { key: 'type', label: 'Type' },
                          { key: 'scope', label: 'Scope' },
                          { key: 'primitive', label: 'Primitive' },
                          { key: 'collectionLike', label: 'Collection' },
                          { key: 'line', label: 'Line' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Object Creations">
                        <DataTable rows={result.objectCreations as unknown as Record<string, unknown>[]} columns={[
                          { key: 'type', label: 'Type' },
                          { key: 'allocationCategory', label: 'Category' },
                          { key: 'line', label: 'Line' },
                          { key: 'memoryImpact', label: 'Memory Impact' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Loops">
                        <DataTable rows={result.loops as unknown as Record<string, unknown>[]} columns={[
                          { key: 'loopType', label: 'Type' },
                          { key: 'riskLevel', label: 'Risk' },
                          { key: 'beginLine', label: 'Start' },
                          { key: 'endLine', label: 'End' },
                          { key: 'reason', label: 'Reason' }
                        ]} />
                      </SectionCard>

                      <SectionCard title="Method Calls">
                        <DataTable rows={result.methodCalls as unknown as Record<string, unknown>[]} columns={[
                          { key: 'methodName', label: 'Method' },
                          { key: 'scope', label: 'Scope' },
                          { key: 'category', label: 'Category' },
                          { key: 'line', label: 'Line' }
                        ]} />
                      </SectionCard>
                    </>
                  )}

                  {tab === 3 && (
                    <Paper variant="outlined" sx={{ p: 2, overflow: 'auto', maxHeight: '70vh' }}>
                      <pre style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{JSON.stringify(result, null, 2)}</pre>
                    </Paper>
                  )}
                </>
              )}
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}
