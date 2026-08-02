import { WidgetType, ValidationError } from '../types/guidewire-pcf';

export class PCFSchemaGuard {
  private static readonly ALLOWED_PARENTS: Record<WidgetType, WidgetType[]> = {
    PCF: [],
    Page: ['PCF'],
    Screen: ['Page', 'WizardStep', 'PCF'],
    WizardStep: ['Page', 'PCF'],
    Toolbar: ['Screen', 'DetailViewPanel'],
    ToolbarButton: ['Toolbar'],
    DetailViewPanel: ['Screen', 'DetailViewTile'],
    DetailViewTile: ['Screen'],
    InputColumn: ['DetailViewPanel', 'DetailViewTile'],
    TextInput: ['InputColumn'],
    SelectInput: ['InputColumn'],
    DateInput: ['InputColumn'],
    CheckBoxInput: ['InputColumn'],
    PasswordInput: ['InputColumn'],
    ButtonInput: ['InputColumn'],
    ListViewTile: ['Screen'],
    ListViewPanel: ['Screen'],
    RowIterator: ['ListViewTile', 'ListViewPanel'],
    Row: ['RowIterator'],
    TextCell: ['Row'],
    DateCell: ['Row'],
    CheckBoxCell: ['Row'],
    FormatCell: ['Row']
  };

  public static isValidParent(childType: WidgetType, parentType: WidgetType): boolean {
    const allowed = this.ALLOWED_PARENTS[childType];
    return !!allowed && allowed.includes(parentType);
  }

  public static validatePlacement(widgetId: string, childType: WidgetType, parentType: WidgetType): ValidationError[] {
    const errors: ValidationError[] = [];
    if (!this.isValidParent(childType, parentType)) {
      const allowed = this.ALLOWED_PARENTS[childType] || [];
      const msg = `Guidewire PCF Schema Error: Widget '<${childType}>' cannot be placed inside '<${parentType}>'. Valid parents: [${allowed.join(', ')}]`;
      errors.push({
        widgetId,
        widgetType: childType,
        parentType,
        message: msg,
        severity: 'ERROR'
      });
    }
    return errors;
  }
}
