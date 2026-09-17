export type Currency = "BRL" | "USD";

export type ReceivableType = "DUPLICATE_MERCANTILE" | "PRE_DATED_CHECK";

export type ReceivableStatus = "AVAILABLE" | "SETTLED";

export interface Receivable {
  id: number;
  assignor: string;
  type: ReceivableType;
  faceValue: number;
  termInMonths: number;
  status: ReceivableStatus;
  dueDate: string;
}

export interface CreateReceivableRequest {
  assignor: string;
  type: ReceivableType;
  faceValue: number;
  termInMonths: number;
  dueDate: string;
}

export interface SettlementRequest {
  receivableId: number;
  paymentCurrency: Currency;
}

export interface Settlement {
  id: number;
  assignor: string;
  faceValue: number;
  type: ReceivableType;
  presentValueBrl: number;
  settledAmount: number;
  paymentCurrency: Currency;
  baseRateUsed: number;
  spreadUsed: number;
  exchangeRateUsed: number | null;
  termInMonths: number;
  createdAt: string;
  receivableId?: number;
}

export interface SimulationRequest {
  assignor: string;
  type: ReceivableType;
  faceValue: number;
  termInMonths: number;
  dueDate: string;
  paymentCurrency: Currency;
}

export interface SimulationResult {
  faceValue: number;
  type: ReceivableType;
  presentValueBrl: number;
  settledAmount: number;
  paymentCurrency: Currency;
  baseRateUsed: number;
  spreadUsed: number;
  exchangeRateUsed: number | null;
  termInMonths: number;
}

export interface ReceivablePage {
  content: Receivable[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
