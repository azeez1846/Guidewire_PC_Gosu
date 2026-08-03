/**
 * Guidewire PolicyCenter Enterprise Features TypeScript Definitions
 */

export type FeatureCategory = 
  | 'Underwriting & Risk'
  | 'Commercial Rating & Retrospective'
  | 'Compliance & Regulatory'
  | 'Specialty Lines'
  | 'Reinsurance & Portfolio';

export interface FieldInputField {
  name: string;
  label: string;
  type: 'string' | 'number' | 'boolean' | 'select';
  defaultValue: any;
  options?: { label: string; value: any }[];
  description: string;
}

export interface FeatureModule {
  id: string;
  title: string;
  category: FeatureCategory;
  shortDescription: string;
  businessPurpose: string;
  endpoint: string;
  method: 'POST' | 'GET';
  inputs: FieldInputField[];
}

export interface ModuleExecutionResult {
  success: boolean;
  timestamp: string;
  endpoint: string;
  data?: any;
  errorMessage?: string;
}
