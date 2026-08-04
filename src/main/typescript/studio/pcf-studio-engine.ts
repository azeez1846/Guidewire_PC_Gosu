import { PCFDocument, PCFNode, WidgetType, ValidationResponse } from '../types/guidewire-pcf';
import { PCFSchemaGuard } from './pcf-schema-guard';
import { GWLogger } from '../GWLogger';

export class PCFStudioEngine {
  private currentPcf: PCFDocument;
  private selectedNode: PCFNode | null = null;
  private isLiveMode = false;

  constructor(initialDocument?: PCFDocument) {
    GWLogger.log('pcf-studio-engine', 'constructor');
    this.currentPcf = initialDocument || {
      id: 'WorkersCompCoveragesDVTile',
      title: 'Workers Compensation Coverages',
      children: [
        {
          id: 'WCLineDV',
          type: 'DetailViewPanel',
          children: [
            {
              id: 'Col1',
              type: 'InputColumn',
              title: 'Employers Liability Limits',
              children: [
                {
                  id: 'EmployersLiabilityLimit',
                  type: 'SelectInput',
                  label: 'Employers Liability Limit',
                  value: 'WCPolicyLine.EmployersLiabilityLimit',
                  required: 'true'
                },
                {
                  id: 'OtherStatesCoverage',
                  type: 'CheckBoxInput',
                  label: 'Other States Coverage (Part Three)',
                  value: 'WCPolicyLine.OtherStatesCoverage'
                },
                {
                  id: 'USLHCoverage',
                  type: 'CheckBoxInput',
                  label: 'US Longshore & Harbor Workers (USL&H)',
                  value: 'WCPolicyLine.USLHCoverage'
                }
              ]
            }
          ]
        }
      ]
    };
  }

  public getDocument(): PCFDocument {
    GWLogger.log('pcf-studio-engine', 'getDocument');
    return this.currentPcf;
  }

  public setDocument(doc: PCFDocument): void {
    GWLogger.log('pcf-studio-engine', 'setDocument');
    this.currentPcf = doc;
  }

  public validateDrop(childType: WidgetType, parentType: WidgetType, widgetId = 'new'): ValidationResponse {
    GWLogger.log('pcf-studio-engine', 'validateDrop');
    const errors = PCFSchemaGuard.validatePlacement(widgetId, childType, parentType);
    if (errors.length === 0) {
      return { valid: true, message: `Valid drop target inside <${parentType}>` };
    }
    return {
      valid: false,
      error: errors[0].message,
      childType,
      parentType
    };
  }

  public generateXML(pcf: PCFDocument = this.currentPcf): string {
    GWLogger.log('pcf-studio-engine', 'generateXML');
    let xml = `<?xml version="1.0" encoding="UTF-8"?>\n<PCF xmlns="http://guidewire.com/pcf" id="${pcf.id}" title="${pcf.title || pcf.id}">\n`;
    xml += this.renderChildrenXML(pcf.children, '  ');
    xml += '</PCF>';
    return xml;
  }

  private renderChildrenXML(children?: PCFNode[], indent = '  '): string {
    GWLogger.log('pcf-studio-engine', 'renderChildrenXML');
    if (!children) return '';
    let res = '';
    children.forEach((c) => {
      if (c.children && c.children.length > 0) {
        res += `${indent}<${c.type} id="${c.id}"${c.title ? ' title="' + c.title + '"' : ''}>\n`;
        res += this.renderChildrenXML(c.children, indent + '  ');
        res += `${indent}</${c.type}>\n`;
      } else {
        res += `${indent}<${c.type} id="${c.id}" label="${c.label || ''}" value="${c.value || ''}"/>\n`;
      }
    });
    return res;
  }
}
