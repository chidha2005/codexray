import { Grid, Paper, Typography } from '@mui/material';
import { AnalysisSummary } from '../types';

interface Props {
  summary: AnalysisSummary;
}

const labels: Array<[keyof AnalysisSummary, string]> = [
  ['classCount', 'Classes'],
  ['methodCount', 'Methods'],
  ['fieldCount', 'Fields'],
  ['parameterCount', 'Parameters'],
  ['localVariableCount', 'Local Variables'],
  ['objectCreationCount', 'Object Creations'],
  ['loopCount', 'Loops'],
  ['conditionCount', 'Conditions'],
  ['methodCallCount', 'Method Calls'],
  ['riskFindingCount', 'Risk Findings']
];

export default function SummaryGrid({ summary }: Props) {
  return (
    <Grid container spacing={2}>
      {labels.map(([key, label]) => (
        <Grid item xs={6} sm={4} md={2.4} key={key}>
          <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h4" fontWeight={800}>{summary[key]}</Typography>
            <Typography variant="caption" color="text.secondary">{label}</Typography>
          </Paper>
        </Grid>
      ))}
    </Grid>
  );
}
