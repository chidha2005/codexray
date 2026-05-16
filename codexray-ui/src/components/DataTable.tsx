import { Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from '@mui/material';
import { ReactNode } from 'react';

interface Props<T> {
  rows: T[];
  columns: Array<{ key: keyof T | string; label: string; render?: (row: T) => ReactNode }>;
  emptyText?: string;
}

export default function DataTable<T extends Record<string, unknown>>({ rows, columns, emptyText = 'No data found.' }: Props<T>) {
  if (!rows || rows.length === 0) {
    return <Typography color="text.secondary">{emptyText}</Typography>;
  }

  return (
    <TableContainer component={Paper} variant="outlined">
      <Table size="small">
        <TableHead>
          <TableRow>
            {columns.map((column) => (
              <TableCell key={String(column.key)} sx={{ fontWeight: 800 }}>{column.label}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row, rowIndex) => (
            <TableRow key={rowIndex} hover>
              {columns.map((column) => (
                <TableCell key={String(column.key)}>
                  {column.render ? column.render(row) : String(row[column.key] ?? '')}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
