package gw.pc.plugin

interface IGosuPlugin {
  public function getPluginName() : String
  public function isAvailable() : boolean
}
