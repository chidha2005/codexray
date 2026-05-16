import { Alert, Box, Chip, Stack, Typography } from '@mui/material';
import { RiskFinding } from '../types';

interface Props {
  risks: RiskFinding[];
}

function color(severity: string): 'error' | 'warning' | 'info' | 'success' {
  if (severity === 'CRITICAL') return 'error';
  if (severity === 'HIGH') return 'warning';
  if (severity === 'MEDIUM') return 'info';
  return 'success';
}

export default function RiskPanel({ risks }: Props) {
  if (risks.length === 0) {
    return <Alert severity="success">No major static memory risks detected in MVP-1 analysis.</Alert>;
  }

  return (
    <Stack spacing={1.5}>
      {risks.map((risk, index) => (
        <Alert key={`${risk.category}-${index}`} severity={color(risk.severity)}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap', mb: 0.5 }}>
            <Typography fontWeight={800}>{risk.title}</Typography>
            <Chip size="small" label={risk.severity} color={color(risk.severity)} />
            <Chip size="small" label={risk.category} variant="outlined" />
            <Chip size="small" label={`Line ${risk.line}`} variant="outlined" />
          </Box>
          <Typography variant="body2">{risk.description}</Typography>
          <Typography variant="body2" sx={{ mt: 0.5 }}><b>Recommendation:</b> {risk.recommendation}</Typography>
        </Alert>
      ))}
    </Stack>
  );
}
