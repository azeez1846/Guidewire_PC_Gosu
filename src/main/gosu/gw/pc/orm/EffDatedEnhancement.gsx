package gw.pc.orm

import com.guidewire.pc.orm.EffDatedBean
import java.util.Date

enhancement EffDatedEnhancement : EffDatedBean {

  public property get IsActiveSlice() : boolean {
    var today = new Date()
    return this.isEffectiveAt(today)
  }

  public property get FixedIdString() : String {
    return this.FixedId != null ? this.FixedId.toString() : "Unassigned"
  }

  public function isSliceEffectiveAt(checkDate : Date) : boolean {
    return this.isEffectiveAt(checkDate)
  }
}
