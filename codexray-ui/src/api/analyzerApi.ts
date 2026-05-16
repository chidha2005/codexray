import axios from 'axios';
import { JavaAnalyzeResponse } from '../types';

const client = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000
});

export async function analyzeJava(sourceCode: string): Promise<JavaAnalyzeResponse> {
  const response = await client.post<JavaAnalyzeResponse>('/api/v1/analyze/java', { sourceCode });
  return response.data;
}
