import { FeatureModule } from './types/features';

export const ENTERPRISE_FEATURES_CATALOG: FeatureModule[] = [
  {
    id: 'ai-underwriting',
    title: 'AI Automated Underwriting Referral & Decision Assistant',
    category: 'Underwriting & Risk',
    shortDescription: 'AI-driven decision assistant evaluating loss history, hazard class codes, and risk scores to provide automated binding recommendations or manager escalation.',
    businessPurpose: 'Accelerates underwriting triage and provides predictive AI risk explanations for complex commercial applications.',
    endpoint: '/rest/v1/ai-referral/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'riskScore', label: 'Risk Score (0-100)', type: 'number', defaultValue: 78, description: 'Composite underwriting risk score' }
    ]
  },
  {
    id: 'esignature-docusign',
    title: 'DocuSign E-Signature Envelope Integration Engine',
    category: 'Compliance & Regulatory',
    shortDescription: 'Generates secure DocuSign e-signature envelope packages for instant digital policy binding and statutory application execution.',
    businessPurpose: 'Automates digital policy binding and application sign-offs via seamless DocuSign API integration.',
    endpoint: '/rest/v1/esignature/create',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'signerEmail', label: 'Signer Email Address', type: 'string', defaultValue: 'policyholder@example.com', description: 'Named Insured email for e-signature' }
    ]
  },
  {
    id: 'geospatial-gis',
    title: 'Geospatial GIS Risk & Wildfire Exposure Service',
    category: 'Reinsurance & Portfolio',
    shortDescription: 'Evaluates GIS latitude/longitude coordinates against live wildfire risk zones, coastal storm surge maps, and sinkhole fault lines.',
    businessPurpose: 'Protects carrier portfolio concentration by evaluating precise property coordinates for natural hazard exposures.',
    endpoint: '/rest/v1/geospatial/risk',
    method: 'POST',
    inputs: [
      { name: 'address', label: 'Property Address', type: 'string', defaultValue: '100 Coastal Hwy, Malibu, CA 90265', description: 'Insured property physical location' }
    ]
  },
  {
    id: 'payment-gateway',
    title: 'Stripe Payment Gateway Installment Processing Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Processes real-time credit card, ACH, and installment payments with tokenized security via Stripe Payment Gateway.',
    businessPurpose: 'Enables direct digital payment collection upon policy quote and renewal binding.',
    endpoint: '/rest/v1/payment/process',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'amount', label: 'Payment Amount ($)', type: 'number', defaultValue: 450.00, description: 'Installment or deposit payment amount' }
    ]
  },
  {
    id: 'vin-lookup',
    title: 'NHTSA VIN Decoder & Vehicle Safety Feature Lookup Engine',
    category: 'Specialty Lines',
    shortDescription: 'Decodes 17-digit VIN numbers to populate vehicle make, model, trim, NHTSA safety ratings, and ADAS anti-theft equipment.',
    businessPurpose: 'Automates vehicle schedule data entry and applies safety equipment rating credits for Commercial and Personal Auto.',
    endpoint: '/rest/v1/vin/decode',
    method: 'POST',
    inputs: [
      { name: 'vin', label: '17-Digit Vehicle VIN', type: 'string', defaultValue: '1G1YC2D45R5100001', description: 'Standard vehicle identification number' }
    ]
  },
  {
    id: 'telematics-ubi',
    title: 'Auto Fleet Telematics Driving Behavior Premium Discount Engine (UBI)',
    category: 'Specialty Lines',
    shortDescription: 'Evaluates driver safety telemetry (hard braking, rapid acceleration, late-night driving) for dynamic UBI discounts up to -20% or surcharges +15%.',
    businessPurpose: 'Commercial & Personal Auto lines use telematics telemetry to reward safe drivers with rate discounts while pricing high-risk behaviors appropriately on renewal.',
    endpoint: '/rest/v1/telematics/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'hardBrakesPer1k', label: 'Hard Brakes / 1k Miles', type: 'number', defaultValue: 1.0, description: 'Events per 1,000 miles driven' },
      { name: 'rapidAccelerationsPer1k', label: 'Rapid Accelerations / 1k Miles', type: 'number', defaultValue: 0.5, description: 'Events per 1,000 miles driven' },
      { name: 'lateNightDrivingPct', label: 'Late Night Driving % (12am-4am)', type: 'number', defaultValue: 0.02, description: 'Fraction of total driving during high-risk hours' },
      { name: 'speedingEventsPer1k', label: 'Speeding Events / 1k Miles', type: 'number', defaultValue: 0.0, description: 'Events exceeding limit by >10mph' }
    ]
  },
  {
    id: 'tria-compliance',
    title: 'TRIA Terrorism Risk Insurance Act Opt-In/Opt-Out Disclosure Engine',
    category: 'Compliance & Regulatory',
    shortDescription: 'Manages mandatory U.S. Federal TRIA 3.5% terrorism surcharge disclosures, opt-in endorsements, and rejection exclusion forms.',
    businessPurpose: 'Federal regulation requires commercial property and liability insurers to disclose certified terrorism coverage terms and attach mandatory TRIA rejection forms if declined.',
    endpoint: '/rest/v1/tria/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'optInTerrorismCoverage', label: 'Opt-In TRIA Coverage', type: 'boolean', defaultValue: true, description: 'Policyholder accepts certified terrorism coverage' },
      { name: 'triaRatePct', label: 'TRIA Surcharge Rate (%)', type: 'number', defaultValue: 0.035, description: 'Standard 3.5% federal terrorism rate' }
    ]
  },
  {
    id: 'pollution-hazard',
    title: 'Environmental & Pollution Liability Hazard Assessment Engine',
    category: 'Specialty Lines',
    shortDescription: 'Assesses Underground Storage Tanks (UST), chemical hazard scores, and proximity to waterways to calculate EIL rating multipliers and containment deductibles.',
    businessPurpose: 'Environmental Impairment Liability (EIL) underwriters evaluate hazardous storage and waterway proximity to enforce mandatory containment deductibles up to $50,000.',
    endpoint: '/rest/v1/pollution/assess',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'ustCount', label: 'Underground Storage Tanks (UST)', type: 'number', defaultValue: 4, description: 'Active tank count on facility grounds' },
      { name: 'chemicalHazardScore', label: 'Chemical Hazard Score (1-10)', type: 'number', defaultValue: 8, description: 'Storage volume and toxic rating score' },
      { name: 'proximityToWaterwayMiles', label: 'Proximity to Waterway (Miles)', type: 'number', defaultValue: 0.4, description: 'Distance to nearest river, lake or coastal bay' },
      { name: 'facilityAgeYears', label: 'Facility Age (Years)', type: 'number', defaultValue: 25, description: 'Age of industrial site structures' }
    ]
  },
  {
    id: 'cyber-liability',
    title: 'Cyber Liability Ransomware & Breach Response Sub-Limit Engine',
    category: 'Specialty Lines',
    shortDescription: 'Evaluates corporate cyber security posture (MFA, daily backups, EDR) and caps ransomware sub-limits at $250k with +30% surcharge if MFA is missing.',
    businessPurpose: 'Cyber underwriters enforce strict security controls like Multi-Factor Authentication (MFA) to prevent catastrophic ransomware losses.',
    endpoint: '/rest/v1/cyber/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'mfaEnabled', label: 'MFA Enabled on All Systems', type: 'boolean', defaultValue: false, description: 'Mandatory Multi-Factor Authentication' },
      { name: 'offlineBackupsDaily', label: 'Daily Offline Backups', type: 'boolean', defaultValue: true, description: 'Immutable daily offsite backups' },
      { name: 'edrDeployed', label: 'EDR Antivirus Deployed', type: 'boolean', defaultValue: true, description: 'Endpoint Detection and Response' },
      { name: 'employeePhishingTrained', label: 'Phishing Awareness Training', type: 'boolean', defaultValue: true, description: 'Quarterly staff phishing simulation training' }
    ]
  },
  {
    id: 'flood-zone-rating',
    title: 'Flood Zone Risk & NFIP Elevation Certificate Premium Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Evaluates FEMA Flood Zones (A, V, X) and Elevation Certificate differentials to grant -30% elevation credits or apply +50% below-BFE surcharges.',
    businessPurpose: 'Commercial property underwriters evaluate FEMA flood maps and structural elevation relative to Base Flood Elevation (BFE) for precise flood pricing.',
    endpoint: '/rest/v1/flood/rate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'floodZone', label: 'FEMA Flood Zone', type: 'select', defaultValue: 'Zone A', options: [{label: 'Zone A (High Risk Inland)', value: 'Zone A'}, {label: 'Zone V (High Risk Coastal)', value: 'Zone V'}, {label: 'Zone X (Low/Moderate Risk)', value: 'Zone X'}], description: 'FEMA flood hazard designation' },
      { name: 'lowestFloorElevationFt', label: 'Lowest Floor Elevation (ft)', type: 'number', defaultValue: 14.0, description: 'Structure lowest floor height above sea level' },
      { name: 'baseFloodElevationBFE', label: 'Base Flood Elevation BFE (ft)', type: 'number', defaultValue: 12.0, description: 'FEMA 100-year flood level height' },
      { name: 'hasFloodProofVents', label: 'Engineered Flood Vents Installed', type: 'boolean', defaultValue: true, description: 'Compliant engineered hydrostatic vents' }
    ]
  },
  {
    id: 'coinsurance-penalty',
    title: 'Property Coinsurance Clause Penalty Engine',
    category: 'Underwriting & Risk',
    shortDescription: 'Evaluates commercial building replacement valuation against 80%/90% coinsurance clauses to apply claim payout penalty reductions if under-insured.',
    businessPurpose: 'Commercial property clauses require buildings to be insured for at least 80-90% of replacement value; under-insured properties incur proportional claim penalties.',
    endpoint: '/rest/v1/coinsurance/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'buildingValue', label: 'Building Replacement Value ($)', type: 'number', defaultValue: 2000000, description: '100% full replacement valuation' },
      { name: 'buildingLimit', label: 'Carried Building Limit ($)', type: 'number', defaultValue: 1200000, description: 'Actual insured policy limit carried' },
      { name: 'coinsurancePct', label: 'Coinsurance Clause %', type: 'number', defaultValue: 0.80, description: 'Mandatory 80% or 90% clause requirement' },
      { name: 'claimLoss', label: 'Claim Property Loss Amount ($)', type: 'number', defaultValue: 500000, description: 'Incurred building property loss' },
      { name: 'deductible', label: 'Property Deductible ($)', type: 'number', defaultValue: 5000, description: 'Policy deductible amount' }
    ]
  },
  {
    id: 'deductible-buyback',
    title: 'Policy Deductible Buyback & Surcharge Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Calculates actuarial buyback surcharge factors when policyholders reduce high deductibles (e.g. buying down $10,000 to $1,000).',
    businessPurpose: 'Allows policyholders to buy down deductible exposure in exchange for a calculated premium surcharge.',
    endpoint: '/rest/v1/deductible/buyback',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'originalDeductible', label: 'Original Deductible ($)', type: 'number', defaultValue: 10000, description: 'Standard baseline deductible' },
      { name: 'targetDeductible', label: 'Target Buyback Deductible ($)', type: 'number', defaultValue: 1000, description: 'Requested reduced deductible' }
    ]
  },
  {
    id: 'uw-escalation',
    title: 'Multi-Tier UW Authority Escalation & Sign-Off Workflow Engine',
    category: 'Underwriting & Risk',
    shortDescription: 'Manages approval hierarchy (Underwriter -> Manager -> VP) enforcing dual sign-offs for TIV > $10M or fraud scores >= 70.',
    businessPurpose: 'Ensures high exposure commercial accounts receive proper governance and senior leadership sign-off prior to policy binding.',
    endpoint: '/rest/v1/uw/escalation',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'totalInsuredValue', label: 'Total Insured Value TIV ($)', type: 'number', defaultValue: 15000000, description: 'Aggregate property & liability value' },
      { name: 'riskScore', label: 'Underwriting Risk Score (0-100)', type: 'number', defaultValue: 75, description: 'Automated hazard risk assessment score' }
    ]
  },
  {
    id: 'sliding-dividend',
    title: 'Loss Sensitive Sliding Scale Policyholder Dividend Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Evaluates commercial retrospective rating plans returning up to 15% dividend returns for low loss ratios (<30%).',
    businessPurpose: 'Rewards commercial accounts that maintain clean safety records with cash dividend refunds after policy expiration.',
    endpoint: '/rest/v1/dividend/calculate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'incurredLosses', label: 'Annual Incurred Loss Amount ($)', type: 'number', defaultValue: 2500, description: 'Total paid + reserved policy claims' }
    ]
  },
  {
    id: 'rate-cap',
    title: 'Renewal Rate Impact Capping & Transition Smoothing Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Enforces maximum annual rate increase caps (e.g. max +10%) on renewal policies to smooth price hikes and prevent customer churn.',
    businessPurpose: 'Carriers cap steep rate increases to maintain high renewal retention while subsidizing transition rates over multiple terms.',
    endpoint: '/rest/v1/rate-cap/apply',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'uncappedProposedPremium', label: 'Uncapped Proposed Premium ($)', type: 'number', defaultValue: 15000, description: 'Raw technical benchmark renewal rate' },
      { name: 'maxRateCapPct', label: 'Max Renewal Cap % (e.g. 0.10)', type: 'number', defaultValue: 0.10, description: 'Maximum allowed annual rate increase cap' }
    ]
  },
  {
    id: 'siu-fraud',
    title: 'SIU Fraud Risk Scoring Engine',
    category: 'Underwriting & Risk',
    shortDescription: 'Analyzes identity anomalies, policy change velocity, and loss history to generate weighted fraud scores and trigger SIU holds.',
    businessPurpose: 'Detects suspicious policy applications and fraudulent claim patterns early to protect carrier loss ratios.',
    endpoint: '/rest/v1/fraud/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' }
    ]
  },
  {
    id: 'reinsurance-ledger',
    title: 'Reinsurance Treaty Layering & Cession Ledger Engine',
    category: 'Reinsurance & Portfolio',
    shortDescription: 'Applies Quota Share, Excess of Loss (XOL), and Catastrophe Treaty layers to partition policy premiums and loss cessions.',
    businessPurpose: 'Automates complex reinsurance accounting, treaty attachment points, layer limits, and reinsurer bordereau reporting.',
    endpoint: '/rest/v1/reinsurance/simulate-loss',
    method: 'POST',
    inputs: [
      { name: 'claimLossAmount', label: 'Claim Loss Amount ($)', type: 'number', defaultValue: 2500000, description: 'Incurred claim loss amount to simulate reinsurance recovery' }
    ]
  },
  {
    id: 'cat-accumulation',
    title: 'Real-Time Catastrophe (CAT) Accumulation Engine',
    category: 'Reinsurance & Portfolio',
    shortDescription: 'Aggregates geospatial Total Insured Value (TIV) across coastal hurricane and earthquake fault zones against carrier exposure caps.',
    businessPurpose: 'Prevents over-concentration of risk in hurricane or wildfire zones by tracking live CAT accumulation totals.',
    endpoint: '/rest/v1/cat/evaluate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'postalCode', label: 'Postal Code', type: 'string', defaultValue: '90210', description: 'Geospatial location ZIP' },
      { name: 'perilZone', label: 'CAT Peril Zone', type: 'string', defaultValue: 'Wildfire_High', description: 'Peril zone category' },
      { name: 'buildingLimit', label: 'Building Limit TIV ($)', type: 'number', defaultValue: 3500000, description: 'Property schedule total value' }
    ]
  },
  {
    id: 'commercial-audit',
    title: 'Commercial Premium Audit & Final Adjustment Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Compares estimated vs actual gross sales/payroll to calculate final audit additional or return premiums.',
    businessPurpose: 'Audits commercial policies post-expiration to adjust premiums based on true operational exposure figures.',
    endpoint: '/rest/v1/audit/process',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'actualExposure', label: 'Actual Audited Payroll ($)', type: 'number', defaultValue: 1200000, description: 'Audited financial exposure' },
      { name: 'estimatedExposure', label: 'Estimated Initial Payroll ($)', type: 'number', defaultValue: 1000000, description: 'Initial binding estimate' },
      { name: 'isNonCompliant', label: 'Non-Compliant Audit', type: 'boolean', defaultValue: false, description: 'Non-cooperative audit surcharge' }
    ]
  },
  {
    id: 'experience-mod',
    title: 'Experience Rating Mod (e-Mod) & NCCI Calculation Engine',
    category: 'Commercial Rating & Retrospective',
    shortDescription: 'Computes Workers Compensation Experience Modification Factor (e-Mod) using NCCI actual vs expected loss formulas.',
    businessPurpose: 'Adjusts Workers Comp premiums based on individual employer claim history relative to industry benchmarks.',
    endpoint: '/rest/v1/emod/calculate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'actualLosses', label: 'Actual Incurred Losses ($)', type: 'number', defaultValue: 17000, description: '3-year policy claim losses' },
      { name: 'expectedLosses', label: 'Expected Benchmark Losses ($)', type: 'number', defaultValue: 20000, description: 'Expected losses for class codes' }
    ]
  },
  {
    id: 'proration-refund',
    title: 'Policy Cancellation Short-Rate vs Pro-Rata Refund Calculator',
    category: 'Compliance & Regulatory',
    shortDescription: 'Calculates unearned premium return refunds comparing standard Pro-Rata factor vs Short-Rate 90% penalty table.',
    businessPurpose: 'Enforces state insurance department cancellation refund rules when policies are cancelled early by carrier or insured.',
    endpoint: '/rest/v1/proration/calculate',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'daysInForce', label: 'Days in Force', type: 'number', defaultValue: 180, description: 'Days policy was active before cancellation' },
      { name: 'totalTermDays', label: 'Total Term Days', type: 'number', defaultValue: 365, description: 'Full policy term length' },
      { name: 'isInsuredInitiated', label: 'Insured Initiated (Short-Rate)', type: 'boolean', defaultValue: true, description: 'Short-rate penalty applies if insured cancels' }
    ]
  },
  {
    id: 'multinational-ledger',
    title: 'Multi-Currency Multinational Local Policy Ledger Engine',
    category: 'Reinsurance & Portfolio',
    shortDescription: 'Manages global master umbrella policy allocations across local foreign currencies (EUR, GBP, JPY) with real-time FX rates.',
    businessPurpose: 'Allows global commercial clients to manage multi-national admitted policies with local currency tax compliance.',
    endpoint: '/rest/v1/multinational/ledger',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' }
    ]
  },
  {
    id: 'commission-split',
    title: 'Multi-Payee Commission Split Engine',
    category: 'Compliance & Regulatory',
    shortDescription: 'Splits gross agency commission across wholesale brokers, managing general agents (MGA), and producing agents.',
    businessPurpose: 'Automates agency accounting and multi-producer commission splits on commercial transactions.',
    endpoint: '/rest/v1/commission/split',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'commissionRate', label: 'Agency Commission Rate (e.g. 0.15)', type: 'number', defaultValue: 0.15, description: 'Gross commission percentage' }
    ]
  },
  {
    id: 'oos-merge',
    title: 'Out-of-Sequence (OOS) Endorsement Transaction Merge Engine',
    category: 'Underwriting & Risk',
    shortDescription: 'Merges effective date endorsement conflicts when backdated policy changes overlap on the policy timeline slice.',
    businessPurpose: 'Guidewire core capability for resolving retroactive policy endorsements without corrupting policy state.',
    endpoint: '/rest/v1/policy/oos-merge',
    method: 'POST',
    inputs: [
      { name: 'policyNumber', label: 'Policy #', type: 'string', defaultValue: 'POL-COMM-1001', description: 'Bound policy number' },
      { name: 'backdatedDate', label: 'Backdated Effective Date', type: 'string', defaultValue: '2026-03-01', description: 'Retroactive endorsement effective date' },
      { name: 'newBiLimit', label: 'New BI Limit', type: 'string', defaultValue: '$2,000,000 / $2,000,000', description: 'Updated Bodily Injury Limit' },
      { name: 'newCollisionDeductible', label: 'New Collision Ded', type: 'string', defaultValue: '$500', description: 'Updated Deductible' }
    ]
  },
  {
    id: 'renewal-eligibility',
    title: 'Pre-Renewal Portfolio Health Batch Process Engine',
    category: 'Reinsurance & Portfolio',
    shortDescription: 'Automated batch process scanning portfolio policies 90 days prior to expiration to score renewal profitability.',
    businessPurpose: 'Pre-screens renewal portfolio to automatically non-renew un-profitable accounts or apply rate increases.',
    endpoint: '/rest/v1/renewal/eligibility',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'proposedRateIncreasePct', label: 'Proposed Renewal Rate Increase (e.g. 0.18)', type: 'number', defaultValue: 0.18, description: 'Target rate increase factor' }
    ]
  },
  {
    id: 'group-coi',
    title: 'Automated Group Account Certificate of Insurance (COI) Issuance',
    category: 'Compliance & Regulatory',
    shortDescription: 'Batch generates ACORD 25 COI documents across multi-location commercial policyholder schedules.',
    businessPurpose: 'Automates mass certificate generation for commercial accounts with hundreds of certificate holders.',
    endpoint: '/rest/v1/coi/issue',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' },
      { name: 'holderName', label: 'Certificate Holder Name', type: 'string', defaultValue: 'General Contractor Inc', description: 'Insured entity requesting certificate' },
      { name: 'holderAddress', label: 'Certificate Holder Address', type: 'string', defaultValue: '100 Construction Way, San Francisco, CA', description: 'Mailing address' },
      { name: 'isAdditionalInsured', label: 'Additional Insured Endorsement', type: 'boolean', defaultValue: true, description: 'Attaches AI coverage endorsement' },
      { name: 'isWaiverOfSubrogation', label: 'Waiver of Subrogation', type: 'boolean', defaultValue: true, description: 'Attaches statutory subrogation waiver' }
    ]
  },
  {
    id: 'forms-inference',
    title: 'Policy Form Inference & Attachment Rules Engine',
    category: 'Compliance & Regulatory',
    shortDescription: 'Evaluates policy coverages, state jurisdictions, and limits to dynamically attach statutory policy forms.',
    businessPurpose: 'Ensures legal policy documents contain all required state mandatory endorsements.',
    endpoint: '/rest/v1/forms/infer',
    method: 'POST',
    inputs: [
      { name: 'jobNumber', label: 'Submission Job #', type: 'string', defaultValue: 'S0001001', description: 'Policy Period Job Number' }
    ]
  },
  {
    id: 'ig-vehicle-details',
    title: 'Guidewire Cloud Integration Gateway (IG) — Vehicle & MVR Vendor Gateway',
    category: 'Specialty Lines',
    shortDescription: 'Integration Gateway (IG) microservice layer executing outbound real-time DMV MVR driver lookup, VIN spec verification, safety scores, and auto underwriting tier recommendations.',
    businessPurpose: 'Connects PolicyCenter Personal Auto and Commercial Auto submissions to external MVR/DMV data vendors via the Integration Gateway microservice JAR.',
    endpoint: '/rest/v1/ig/vehicle-details',
    method: 'POST',
    inputs: [
      { name: 'vin', label: 'Vehicle VIN', type: 'string', defaultValue: '1FA6P8CF0R5100001', description: '17-character VIN identifier' },
      { name: 'vehicleYear', label: 'Vehicle Model Year', type: 'number', defaultValue: 2025, description: 'Model manufacture year' },
      { name: 'vehicleMake', label: 'Vehicle Make', type: 'string', defaultValue: 'Ford', description: 'Manufacturer make' },
      { name: 'vehicleModel', label: 'Vehicle Model', type: 'string', defaultValue: 'Mustang GT', description: 'Model designation' },
      { name: 'driverLicenseNumber', label: 'Driver License Number', type: 'string', defaultValue: 'DL-CA-9948123', description: 'Driver license (Use DL-DUI-9948000 for high-risk test)' },
      { name: 'driverState', label: 'Driver State Jurisdiction', type: 'string', defaultValue: 'CA', description: 'State licensing authority' }
    ]
  }
];
