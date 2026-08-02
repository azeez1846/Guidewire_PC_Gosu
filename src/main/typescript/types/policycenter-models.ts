export interface Account {
  accountNumber: string;
  accountHolderName: string;
  accountHolderType: 'Company' | 'Individual';
  fein?: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  phone?: string;
  email?: string;
  producerCode: string;
  industryCode?: string;
  accountStatus: 'Pending' | 'Active' | 'Closed';
}

export interface PolicyPeriod {
  jobNumber: string;
  jobType: 'Submission' | 'PolicyChange' | 'Cancellation' | 'Renewal';
  policyNumber?: string;
  productCode: 'WorkersComp' | 'CommercialAuto' | 'GeneralLiability' | 'CommercialProperty' | 'CommercialUmbrella';
  effectiveDate: string;
  expirationDate: string;
  status: 'Draft' | 'Quoted' | 'Bound' | 'Issued' | 'Cancelled';
  totalPremium: number;
  baseState: string;
  accountNumber: string;
}

export interface WorkersCompLine {
  totalPayrollExposure: number;
  experienceMod: number;
  governingClassCode: string;
  employersLiabilityLimit: string;
  otherStatesCoverage: boolean;
  uslhCoverage: boolean;
  safetyProgramDiscount: number;
}

export interface CommercialAutoLine {
  fleetType: 'Commercial' | 'NonFleet';
  businessAutoType: 'Service' | 'Commercial' | 'Retail';
  radiusOfOperation: 'Local' | 'Intermediate' | 'LongDistance';
  numVehicles: number;
  hiredAutoCoverage: boolean;
  nonOwnedAutoCoverage: boolean;
}

export interface GeneralLiabilityLine {
  occurrenceLimit: number;
  aggregateLimit: number;
  productsCompOpsLimit: number;
  personalAdvInjuryLimit: number;
  fireDamageLimit: number;
  medicalExpensesLimit: number;
  premisesOperationsExposure: number;
}
