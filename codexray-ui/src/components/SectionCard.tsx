import { Card, CardContent, Typography } from '@mui/material';
import { ReactNode } from 'react';

interface Props {
  title: string;
  children: ReactNode;
}

export default function SectionCard({ title, children }: Props) {
  return (
    <Card elevation={0} sx={{ border: '1px solid rgba(255,255,255,0.08)', mb: 2 }}>
      <CardContent>
        <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>{title}</Typography>
        {children}
      </CardContent>
    </Card>
  );
}
