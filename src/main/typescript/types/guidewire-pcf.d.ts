export type WidgetType =
  | 'PCF'
  | 'Screen'
  | 'Page'
  | 'WizardStep'
  | 'DetailViewPanel'
  | 'DetailViewTile'
  | 'InputColumn'
  | 'ListViewTile'
  | 'ListViewPanel'
  | 'RowIterator'
  | 'Row'
  | 'Toolbar'
  | 'TextInput'
  | 'SelectInput'
  | 'DateInput'
  | 'CheckBoxInput'
  | 'PasswordInput'
  | 'ButtonInput'
  | 'TextCell'
  | 'DateCell'
  | 'CheckBoxCell'
  | 'FormatCell'
  | 'ToolbarButton';

export interface PCFNode {
  id: string;
  type: WidgetType;
  label?: string;
  value?: string;
  title?: string;
  required?: string | boolean;
  editable?: string | boolean;
  action?: string;
  children?: PCFNode[];
}

export interface PCFDocument {
  id: string;
  title?: string;
  children: PCFNode[];
}

export interface ValidationError {
  widgetId: string;
  widgetType: WidgetType;
  parentType: WidgetType;
  message: string;
  severity: 'ERROR' | 'WARNING';
}

export interface ValidationResponse {
  valid: boolean;
  error?: string;
  message?: string;
  childType?: string;
  parentType?: string;
}
