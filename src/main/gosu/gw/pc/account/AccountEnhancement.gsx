package gw.pc.account

import com.guidewire.pc.model.Account

enhancement AccountEnhancement : Account {

  public property get FormattedAddress() : String {
    var sb = new java.lang.StringBuilder()
    sb.append(this.AddressLine1)
    if (this.AddressLine2 != null and this.AddressLine2.trim().length() > 0) {
      sb.append(", ").append(this.AddressLine2)
    }
    sb.append(", ").append(this.City).append(", ").append(this.State).append(" ").append(this.PostalCode)
    return sb.toString()
  }

  public property get DisplayName() : String {
    return this.AccountNumber + " - " + this.AccountHolderName
  }

  public function canCreateSubmission() : boolean {
    return this.AccountStatus == "Active" or this.AccountStatus == "Pending"
  }

  public function validateAccount() : java.util.List<String> {
    var errors = new java.util.ArrayList<String>()
    if (this.AccountHolderName == null or this.AccountHolderName.trim().length() == 0) {
      errors.add("Account Holder Name is required.")
    }
    if (this.AddressLine1 == null or this.AddressLine1.trim().length() == 0) {
      errors.add("Address Line 1 is required.")
    }
    if (this.City == null or this.City.trim().length() == 0) {
      errors.add("City is required.")
    }
    if (this.State == null or this.State.trim().length() == 0) {
      errors.add("State is required.")
    }
    if (this.PostalCode == null or this.PostalCode.trim().length() == 0) {
      errors.add("ZIP / Postal Code is required.")
    }
    if (this.ProducerCode == null or this.ProducerCode.trim().length() == 0) {
      errors.add("Producer Code is required.")
    }
    return errors
  }
}
