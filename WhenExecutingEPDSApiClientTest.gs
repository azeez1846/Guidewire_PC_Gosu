package xlp.integration.epdsnameclearance

uses feign.FeignException
uses gw.account.AccountSearchCriteria
uses gw.api.locale.DisplayKey
uses gw.api.properties.RuntimePropertyRetriever
uses gw.restclient.config.Config
uses gw.testharness.v3.Suites
uses gwgen.epds.api.EpdsApi
uses gwgen.epds.model.ContractDataSyncRequest
uses gwgen.epds.model.ContractDataSyncTransformResponse
uses gwgen.epds.model.PartyDataSearchRequest
uses gwgen.epds.model.PartyDataSearchResponse
uses org.mockito.Mockito
uses xlp.common.PCServerTestClassBase_Ext
uses xlp.integration.common.CommonConstants
uses xlp.integration.epds.client.EpdsApiClient
uses xlp.integration.epds.helper.EpdsApiHelper
uses xlp.suites.IntegrationSuite
uses xlp.web.EPDSSearchMapperDTO
uses xlp.web.EPDSSearchProcessor

@Export
@Suites(IntegrationSuite.NAME)
class WhenExecutingEPDSApiClientTest extends PCServerTestClassBase_Ext {
  protected var mockEPDSApiHelper : MockEpdsApiHelper
  var epdsSearchProcessor : EPDSSearchProcessor
  var epdsSearchMapperDTO : EPDSSearchMapperDTO
  var accountSearchCriteria : AccountSearchCriteria

  override function beforeMethod() {
    epdsSearchProcessor = new EPDSSearchProcessor()
    epdsSearchMapperDTO = new EPDSSearchMapperDTO()
    accountSearchCriteria = new AccountSearchCriteria()
  }

  // ===================================================================
  // 1. Search Processor & Mock Helper End-to-End Tests
  // ===================================================================

  /**
   * Positive Case - EPDS Search Party Service successfully returns party data.
   */
  function testEPDSSearchPartyWithSuccess() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    mockEPDSApiHelper = new MockEpdsApiHelper()
    var partySearchResponse = mockEPDSApiHelper.epdsPartySearch(accountSearchCriteria)

    assertTrue(partySearchResponse.Parties.HasElements)
    var epdsSearchMapperList = epdsSearchProcessor.populateEPDSSearchMapperList(partySearchResponse)
    assertTrue(epdsSearchMapperList.HasElements)

    var entryHavingPartyContracts = epdsSearchMapperList.firstWhere(\dto -> dto.PartyContracts.HasElements)
    assertEquals(epdsSearchProcessor.getExistingPoliciesValue(entryHavingPartyContracts), DisplayKey.get("Xlp.Account.EPDS.Policies_Ext"))

    var entryWithoutPartyContracts = epdsSearchMapperList.firstWhere(\dto -> not dto.PartyContracts.HasElements)
    assertEquals(epdsSearchProcessor.getExistingPoliciesValue(entryWithoutPartyContracts), xlp.util.CommonConstants.EMPTY)

    assertNotNull(epdsSearchProcessor.getAddress(entryHavingPartyContracts))
  }

  /**
   * Negative Case - EPDS Search Party Service did not find searched party data.
   */
  function testEPDSSearchPartyWithFailure() {
    accountSearchCriteria.CompanyName = "TestUser123"
    accountSearchCriteria.Country = Country.TC_US
    mockEPDSApiHelper = new MockEpdsApiHelper()
    var partySearchResponse = mockEPDSApiHelper.epdsPartySearch(accountSearchCriteria)

    assertTrue(partySearchResponse.ErrorMessage == "No party record found for search criteria.")
    var epdsSearchMapperList = epdsSearchProcessor.populateEPDSSearchMapperList(partySearchResponse)
    assertFalse(epdsSearchMapperList.HasElements)
  }

  /**
   * Positive scenario - AccountSearchCriteria values cleared when matched in EPDSSearchMapperDTO
   */
  function testClearMethodConditionSatisfied() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    accountSearchCriteria.OfficialDUNSID = "1234567"
    accountSearchCriteria.OfficialId = "123456789"

    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)

    epdsSearchMapperDTO.IsSelected = false
    epdsSearchMapperDTO.OrganizationName = "Ford"

    epdsSearchProcessor.clearMethod(accountSearchCriteria, epdsSearchMapperDTO)

    assertNull(accountSearchCriteria.OfficialDUNSID)
    assertNull(accountSearchCriteria.OfficialId)
  }

  /**
   * Negative scenario - AccountSearchCriteria values not cleared when names do not match
   */
  function testClearMethodConditionNotSatisfied() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    accountSearchCriteria.OfficialDUNSID = "1234567"
    accountSearchCriteria.OfficialId = "123456789"

    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)

    epdsSearchMapperDTO.IsSelected = false
    epdsSearchMapperDTO.OrganizationName = "Tesla"

    epdsSearchProcessor.clearMethod(accountSearchCriteria, epdsSearchMapperDTO)

    assertEquals("1234567", accountSearchCriteria.OfficialDUNSID)
    assertEquals("123456789", accountSearchCriteria.OfficialId)
  }

  /**
   * Positive Case - Address formatting with County populated.
   */
  function testGetAddressWithCountyNotNull() {
    epdsSearchMapperDTO.AddressLine1 = "3977 abcd St"
    epdsSearchMapperDTO.City = "AAAA"
    epdsSearchMapperDTO.State = "CA"
    epdsSearchMapperDTO.County = "New County"
    epdsSearchMapperDTO.PostalCode = "92921"
    var address = epdsSearchProcessor.getAddress(epdsSearchMapperDTO)
    assertEquals("3977 abcd StAAAANew CountyCA92921", address)
  }

  /**
   * Positive Case - Address formatting without County populated.
   */
  function testGetAddressWithNoCounty() {
    epdsSearchMapperDTO.AddressLine1 = "3977 abcd St"
    epdsSearchMapperDTO.City = "AAAA"
    epdsSearchMapperDTO.State = "CA"
    epdsSearchMapperDTO.PostalCode = "92921"
    var address = epdsSearchProcessor.getAddress(epdsSearchMapperDTO)
    assertEquals("3977 abcd StAAAACA92921", address)
  }

  function testEpdsSearchPartySuccessWithDirectRequest() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var req = new PartyDataSearchRequest() {
      :GlobalTrackingId = UUID.randomUUID().toString(),
      :OrgName = accountSearchCriteria.CompanyName,
      :IncludeContract = "true",
      :Country = accountSearchCriteria.Country as String,
      :NoOfOccurances = 1
    }
    var helper = new MockEpdsApiHelper()
    var epdsSearchPartyResp = helper.invokeEpdsPartySearch(req)
    assertTrue(epdsSearchPartyResp.Parties.HasElements)
  }

  function testEpdsSearchPartyExceptionWithFailingMockHelper() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var failingHelper = new FailingMockEpdsApiHelper()
    var partySearchResponse = failingHelper.epdsPartySearch(accountSearchCriteria)
    assertNull(partySearchResponse.getErrorMessage())
  }

  function testEpdsSearchPartyFailureWithFailingMockClient() {
    accountSearchCriteria.CompanyName = "Ford"
    accountSearchCriteria.Country = Country.TC_US
    var req = new PartyDataSearchRequest() {
      :GlobalTrackingId = UUID.randomUUID().toString(),
      :OrgName = accountSearchCriteria.CompanyName,
      :IncludeContract = "true",
      :Country = accountSearchCriteria.Country as String,
      :NoOfOccurances = 1
    }
    var failingClient = new FailingMockEpdsApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))
    var caught = false
    try {
      failingClient.epdsSearchPartyData(req)
    } catch (ex : Exception) {
      caught = true
      assertEquals("Forced Exception", ex.getMessage())
    }
    assertTrue(caught)
  }

  // ===================================================================
  // 2. Response DTO
  // ===================================================================

  function testContractDataSyncTransformResponseGettersAndSetters() {
    var response = new xlp.integration.epds.response.ContractDataSyncTransformResponse()
    assertNull(response.getErrorMessage())
    assertNull(response.getMessage())

    response.setErrorMessage("Validation Error")
    response.setMessage("Contract Sync Processed")

    assertEquals("Validation Error", response.getErrorMessage())
    assertEquals("Contract Sync Processed", response.getMessage())
  }

  // ===================================================================
  // 3. EpdsApiHelper - Business Logic & Mappings
  // ===================================================================

  function testMapContractStatusCodeAllBranches() {
    var cancelBoundPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(cancelBoundPol.Job.Subtype).thenReturn(typekey.Job.TC_CANCELLATION)
    Mockito.when(cancelBoundPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_BOUND)
    assertEquals(CommonConstants.CDS_STATUS_CAN, EpdsApiHelper.mapContractStatusCode(cancelBoundPol))

    var cancelDraftPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(cancelDraftPol.Job.Subtype).thenReturn(typekey.Job.TC_CANCELLATION)
    Mockito.when(cancelDraftPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_DRAFT)
    assertEquals(CommonConstants.CDS_STATUS_BWO, EpdsApiHelper.mapContractStatusCode(cancelDraftPol))

    var draftPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(draftPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_DRAFT)
    assertEquals(CommonConstants.CDS_STATUS_BWO, EpdsApiHelper.mapContractStatusCode(draftPol))

    var rescindedPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(rescindedPol.Status).thenReturn(PolicyPeriodStatus.TC_RESCINDED)
    assertEquals(CommonConstants.CDS_STATUS_BND, EpdsApiHelper.mapContractStatusCode(rescindedPol))

    var boundPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(boundPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_BOUND)
    assertEquals(CommonConstants.CDS_STATUS_BND, EpdsApiHelper.mapContractStatusCode(boundPol))

    var expiredPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(expiredPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_EXPIRED)
    assertEquals(CommonConstants.CDS_STATUS_EXP, EpdsApiHelper.mapContractStatusCode(expiredPol))

    var declinedPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(declinedPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_DECLINED)
    assertEquals(CommonConstants.CDS_STATUS_DEC, EpdsApiHelper.mapContractStatusCode(declinedPol))

    var quotedPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(quotedPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_QUOTED)
    assertEquals(CommonConstants.CDS_STATUS_QTE, EpdsApiHelper.mapContractStatusCode(quotedPol))

    var nonRenewedPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nonRenewedPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_NONRENEWED)
    assertEquals(CommonConstants.CDS_STATUS_NRW, EpdsApiHelper.mapContractStatusCode(nonRenewedPol))

    var notTakenPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(notTakenPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_NOTTAKEN)
    assertEquals(CommonConstants.CDS_STATUS_QTL, EpdsApiHelper.mapContractStatusCode(notTakenPol))

    var withdrawnPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(withdrawnPol.Status).thenReturn(typekey.PolicyPeriodStatus.TC_WITHDRAWN)
    assertEquals(CommonConstants.CDS_STATUS_IAC, EpdsApiHelper.mapContractStatusCode(withdrawnPol))

    var defaultPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(defaultPol.Status).thenReturn(null)
    assertNull(EpdsApiHelper.mapContractStatusCode(defaultPol))
  }

  function testGetLobCodeAllLineSubtypesAndSubProducts() {
    // GL7
    var gl7Line = Mockito.mock(productmodel.GL7Line)
    Mockito.when(gl7Line.Subtype).thenReturn(typekey.PolicyLine.TC_GL7GENERALLIABILITYLINE)
    var gl7Pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(gl7Pol.Lines).thenReturn(new entity.PolicyLine[]{gl7Line})
    assertEquals(CommonConstants.CDS_GL7, EpdsApiHelper.getLobCode(gl7Pol))

    // WCM
    var wcmLine = Mockito.mock(productmodel.WCMLine)
    Mockito.when(wcmLine.Subtype).thenReturn(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE)
    var wcmPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wcmPol.Lines).thenReturn(new entity.PolicyLine[]{wcmLine})
    assertEquals(CommonConstants.CDS_WCM, EpdsApiHelper.getLobCode(wcmPol))

    // CA7
    var ca7Line = Mockito.mock(productmodel.CA7Line)
    Mockito.when(ca7Line.Subtype).thenReturn(typekey.PolicyLine.TC_CA7COMMAUTOLINE)
    var ca7Pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(ca7Pol.Lines).thenReturn(new entity.PolicyLine[]{ca7Line})
    assertEquals(CommonConstants.CDS_CA7, EpdsApiHelper.getLobCode(ca7Pol))

    // PSL
    var pslLine = Mockito.mock(productmodel.PropertySelectLine)
    Mockito.when(pslLine.Subtype).thenReturn(typekey.PolicyLine.TC_PSLPROPERTYSELECTLINE)
    var pslPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pslPol.Lines).thenReturn(new entity.PolicyLine[]{pslLine})
    assertEquals(CommonConstants.CDS_PSL, EpdsApiHelper.getLobCode(pslPol))

    // CUL
    var culLine = Mockito.mock(productmodel.CommercialUmbrellaLine)
    Mockito.when(culLine.Subtype).thenReturn(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE)
    var culPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(culPol.Lines).thenReturn(new entity.PolicyLine[]{culLine})
    assertEquals(CommonConstants.CDS_CU, EpdsApiHelper.getLobCode(culPol))

    // WPL Primary Casualty subproducts
    var wplLine = Mockito.mock(productmodel.PrimaryCasualtyLine)
    Mockito.when(wplLine.Subtype).thenReturn(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE)

    var wplCasPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wplCasPol.Lines).thenReturn(new entity.PolicyLine[]{wplLine})
    Mockito.when(wplCasPol.PrimaryCasualtyLine.SubProduct).thenReturn(WPLSubproduct.TC_PRIMARYCASUALTY)
    assertEquals(CommonConstants.CDS_GL7, EpdsApiHelper.getLobCode(wplCasPol))

    var wplEnergyPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wplEnergyPol.Lines).thenReturn(new entity.PolicyLine[]{wplLine})
    Mockito.when(wplEnergyPol.PrimaryCasualtyLine.SubProduct).thenReturn(WPLSubproduct.TC_PRIMARYENERGY)
    assertEquals(CommonConstants.CDS_ENERGY, EpdsApiHelper.getLobCode(wplEnergyPol))

    // WEL Excess Casualty subproducts
    var welLine = Mockito.mock(productmodel.ExcessCasualtyLine)
    Mockito.when(welLine.Subtype).thenReturn(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE)

    var welCasPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welCasPol.Lines).thenReturn(new entity.PolicyLine[]{welLine})
    Mockito.when(welCasPol.ExcessCasualtyLineExists).thenReturn(true)
    Mockito.when(welCasPol.ExcessCasualtyLine.ExcessSubProduct).thenReturn(WELSubProduct.TC_EXCESSCASUALTY)
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welCasPol))

    var welEnergyPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welEnergyPol.Lines).thenReturn(new entity.PolicyLine[]{welLine})
    Mockito.when(welEnergyPol.ExcessCasualtyLineExists).thenReturn(true)
    Mockito.when(welEnergyPol.ExcessCasualtyLine.ExcessSubProduct).thenReturn(WELSubProduct.TC_EXCESSENERGY)
    assertEquals(CommonConstants.CDS_EC, EpdsApiHelper.getLobCode(welEnergyPol))

    var welNotExistsPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welNotExistsPol.Lines).thenReturn(new entity.PolicyLine[]{welLine})
    Mockito.when(welNotExistsPol.ExcessCasualtyLineExists).thenReturn(false)
    assertNull(EpdsApiHelper.getLobCode(welNotExistsPol))

    // Default branch
    var defaultLine = Mockito.mock(productmodel.GL7Line)
    Mockito.when(defaultLine.Subtype).thenReturn(null)
    var defaultPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(defaultPol.Lines).thenReturn(new entity.PolicyLine[]{defaultLine})
    assertNull(EpdsApiHelper.getLobCode(defaultPol))
  }

  function testFindLastSubmissionNumWithWithdrawnStatus() {
    var now = gw.api.util.DateUtil.currentDate()
    var earlier = gw.api.util.DateUtil.addYears(now, -1)

    var matchingPeriod = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(matchingPeriod.Status).thenReturn(PolicyPeriodStatus.TC_BOUND)
    Mockito.when(matchingPeriod.TermNumber).thenReturn(2)
    Mockito.when(matchingPeriod.PeriodStart).thenReturn(now)
    Mockito.when(matchingPeriod.EditEffectiveDate).thenReturn(now)
    Mockito.when(matchingPeriod.ModelDate).thenReturn(now)
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("SUB-201")

    var wrongTermPeriod = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wrongTermPeriod.Status).thenReturn(PolicyPeriodStatus.TC_BOUND)
    Mockito.when(wrongTermPeriod.TermNumber).thenReturn(1)
    Mockito.when(wrongTermPeriod.PeriodStart).thenReturn(earlier)
    Mockito.when(wrongTermPeriod.EditEffectiveDate).thenReturn(earlier)
    Mockito.when(wrongTermPeriod.ModelDate).thenReturn(earlier)
    Mockito.when(wrongTermPeriod.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)

    var cancelPeriod = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(cancelPeriod.Status).thenReturn(PolicyPeriodStatus.TC_BOUND)
    Mockito.when(cancelPeriod.TermNumber).thenReturn(2)
    Mockito.when(cancelPeriod.PeriodStart).thenReturn(now)
    Mockito.when(cancelPeriod.EditEffectiveDate).thenReturn(now)
    Mockito.when(cancelPeriod.ModelDate).thenReturn(now)
    Mockito.when(cancelPeriod.Job.Subtype).thenReturn(typekey.Job.TC_CANCELLATION)

    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_WITHDRAWN)
    Mockito.when(pol.Policy.Periods).thenReturn(new PolicyPeriod[]{wrongTermPeriod, cancelPeriod, matchingPeriod})
    assertEquals("SUB-201", EpdsApiHelper.findLastSubmissionNum(pol))

    // Test rewrite job match
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_REWRITE)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("REW-202")
    assertEquals("REW-202", EpdsApiHelper.findLastSubmissionNum(pol))

    // Test renewal job match
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_RENEWAL)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("REN-203")
    assertEquals("REN-203", EpdsApiHelper.findLastSubmissionNum(pol))
  }

  function testFindLastSubmissionNumWithTermNumberGreaterThanOne() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(pol.TermNumber).thenReturn(3)

    var matchingPeriod = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(matchingPeriod.TermNumber).thenReturn(2)
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("SUB-101")

    var wrongTermPeriod = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wrongTermPeriod.TermNumber).thenReturn(1)

    Mockito.when(pol.Policy.Periods).thenReturn(new PolicyPeriod[]{wrongTermPeriod, matchingPeriod})
    assertEquals("SUB-101", EpdsApiHelper.findLastSubmissionNum(pol))

    // Test rewrite match
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_REWRITE)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("REW-102")
    assertEquals("REW-102", EpdsApiHelper.findLastSubmissionNum(pol))

    // Test renewal match
    Mockito.when(matchingPeriod.Job.Subtype).thenReturn(typekey.Job.TC_RENEWAL)
    Mockito.when(matchingPeriod.Job.JobNumber).thenReturn("REN-103")
    assertEquals("REN-103", EpdsApiHelper.findLastSubmissionNum(pol))
  }

  function testFindLastSubmissionNumWithTermNumberOneOrLess() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(pol.TermNumber).thenReturn(1)

    assertNull(EpdsApiHelper.findLastSubmissionNum(pol))
  }

  function testIsContractDataSyncCallEligibleAllBranches() {
    var multiStatuses = {
      PolicyPeriodStatus.TC_QUOTED,
      PolicyPeriodStatus.TC_DRAFT,
      PolicyPeriodStatus.TC_EXPIRED,
      PolicyPeriodStatus.TC_NOTTAKEN,
      PolicyPeriodStatus.TC_WITHDRAWN
    }

    for (st in multiStatuses) {
      var subPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
      Mockito.when(subPol.Status).thenReturn(st)
      Mockito.when(subPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
      Mockito.when(subPol.Job.Underwriter.PublicID).thenReturn("uw1")
      Mockito.when(subPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
      Mockito.when(subPol.Policy.Account.XLNameId).thenReturn("X123")
      Mockito.when(subPol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc1")
      Mockito.when(subPol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn("X456")
      assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(subPol))

      var renPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
      Mockito.when(renPol.Status).thenReturn(st)
      Mockito.when(renPol.Job.Subtype).thenReturn(typekey.Job.TC_RENEWAL)
      Mockito.when(renPol.Job.Underwriter.PublicID).thenReturn("uw1")
      Mockito.when(renPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
      Mockito.when(renPol.Policy.Account.XLNameId).thenReturn("X123")
      Mockito.when(renPol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc1")
      Mockito.when(renPol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn("X456")
      assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(renPol))

      var canPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
      Mockito.when(canPol.Status).thenReturn(st)
      Mockito.when(canPol.Job.Subtype).thenReturn(typekey.Job.TC_CANCELLATION)
      assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(canPol))
    }

    // TC_DECLINED
    var decSubPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(decSubPol.Status).thenReturn(PolicyPeriodStatus.TC_DECLINED)
    Mockito.when(decSubPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(decSubPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(decSubPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
    Mockito.when(decSubPol.Policy.Account.XLNameId).thenReturn("X123")
    Mockito.when(decSubPol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc1")
    Mockito.when(decSubPol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn("X456")
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(decSubPol))

    var decRenPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(decRenPol.Status).thenReturn(PolicyPeriodStatus.TC_DECLINED)
    Mockito.when(decRenPol.Job.Subtype).thenReturn(typekey.Job.TC_RENEWAL)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(decRenPol))

    // TC_NONRENEWED
    var nonRenPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nonRenPol.Status).thenReturn(PolicyPeriodStatus.TC_NONRENEWED)
    Mockito.when(nonRenPol.Job.Subtype).thenReturn(typekey.Job.TC_RENEWAL)
    Mockito.when(nonRenPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(nonRenPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
    Mockito.when(nonRenPol.Policy.Account.XLNameId).thenReturn("X123")
    Mockito.when(nonRenPol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc1")
    Mockito.when(nonRenPol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn("X456")
    assertTrue(EpdsApiHelper.isContractDataSyncCallEligible(nonRenPol))

    var nonRenSubPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nonRenSubPol.Status).thenReturn(PolicyPeriodStatus.TC_NONRENEWED)
    Mockito.when(nonRenSubPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nonRenSubPol))

    // Ineligible status (e.g. TC_BOUND)
    var boundPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(boundPol.Status).thenReturn(PolicyPeriodStatus.TC_BOUND)
    Mockito.when(boundPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(boundPol))

    // Null checks when eligible:
    var nullUwPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nullUwPol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(nullUwPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(nullUwPol.Job.Underwriter).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullUwPol))

    var nullPniPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nullPniPol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(nullPniPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(nullPniPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(nullPniPol.PrimaryNamedInsured).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullPniPol))

    var nullAcctIdPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nullAcctIdPol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(nullAcctIdPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(nullAcctIdPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(nullAcctIdPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
    Mockito.when(nullAcctIdPol.Policy.Account.XLNameId).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullAcctIdPol))

    var nullProdPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nullProdPol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(nullProdPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(nullProdPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(nullProdPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
    Mockito.when(nullProdPol.Policy.Account.XLNameId).thenReturn("X123")
    Mockito.when(nullProdPol.EffectiveDatedFields.ProducerCode).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullProdPol))

    var nullBrokerIdPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(nullBrokerIdPol.Status).thenReturn(PolicyPeriodStatus.TC_QUOTED)
    Mockito.when(nullBrokerIdPol.Job.Subtype).thenReturn(typekey.Job.TC_SUBMISSION)
    Mockito.when(nullBrokerIdPol.Job.Underwriter.PublicID).thenReturn("uw1")
    Mockito.when(nullBrokerIdPol.PrimaryNamedInsured.PublicID).thenReturn("pni1")
    Mockito.when(nullBrokerIdPol.Policy.Account.XLNameId).thenReturn("X123")
    Mockito.when(nullBrokerIdPol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc1")
    Mockito.when(nullBrokerIdPol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn(null)
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(nullBrokerIdPol))
  }

  function testMapPartyDataAllAssociations() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Policy.Account.XLNameId).thenReturn("X52058205")
    Mockito.when(pol.PrimaryNamedInsured.PublicID).thenReturn("pni-1")
    Mockito.when(pol.PrimaryNamedInsured.DisplayName).thenReturn("Ford Motor Co")

    Mockito.when(pol.ProducerCodeOfRecord.XLNameID_Ext).thenReturn("X99887766")
    Mockito.when(pol.EffectiveDatedFields.ProducerCode.PublicID).thenReturn("pc-1")
    Mockito.when(pol.producerCodeOfRecordValue(pol)).thenReturn("PC Record Val")

    Mockito.when(pol.Job.Underwriter.PublicID).thenReturn("uw-1")
    Mockito.when(pol.Job.Underwriter.DisplayName).thenReturn("Jane UW")

    var associations = EpdsApiHelper.mapPartyData(pol)

    assertEquals(3, associations.size())
    assertEquals(CommonConstants.PARTY_ASSOC_1, associations.get(0).PartyAssociationId)
    assertEquals(CommonConstants.INSURED, associations.get(0).PartyRoleTypeCode)
    assertEquals("X52058205", associations.get(0).Party.PartyIdentifier)
    assertEquals("pni-1", associations.get(0).Party.PartyId)
    assertEquals(CommonConstants.ORG, associations.get(0).Party.PartyTypeCode)
    assertEquals("Ford Motor Co", associations.get(0).Party.DisplayName)

    assertEquals(CommonConstants.PARTY_ASSOC_2, associations.get(1).PartyAssociationId)
    assertEquals(CommonConstants.BROKER, associations.get(1).PartyRoleTypeCode)
    assertEquals("X99887766", associations.get(1).Party.PartyIdentifier)
    assertEquals("pc-1", associations.get(1).Party.PartyId)

    assertEquals(CommonConstants.PARTY_ASSOC_3, associations.get(2).PartyAssociationId)
    assertEquals(CommonConstants.UW, associations.get(2).PartyRoleTypeCode)
    assertEquals("uw-1", associations.get(2).Party.PartyId)
    assertEquals("Jane UW", associations.get(2).Party.DisplayName)
  }

  function testFormatNumericValuesAllBranches() {
    assertEquals("0.000", EpdsApiHelper.formatNumericValues(null))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues(""))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues("   "))
    assertEquals("0.000", EpdsApiHelper.formatNumericValues("$---"))
    assertEquals("2.500", EpdsApiHelper.formatNumericValues("$2,500,000"))
    assertEquals("1.000", EpdsApiHelper.formatNumericValues("1000000"))
    assertEquals("0.500", EpdsApiHelper.formatNumericValues("500,000"))
  }

  function testGetLimitsAndGetAttachmentPointAllBranches() {
    // WCM
    var wcmLine = Mockito.mock(productmodel.WCMLine)
    Mockito.when(wcmLine.Subtype).thenReturn(typekey.PolicyLine.TC_WCMWORKERSCOMPLINE)
    Mockito.when(wcmLine.WCMLineDiseasePolicyLimit).thenReturn("1,000,000")
    var wcmPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wcmPol.Lines).thenReturn(new entity.PolicyLine[]{wcmLine})
    Mockito.when(wcmPol.WCMLine).thenReturn(wcmLine)
    assertEquals("1.000", EpdsApiHelper.getLimits(wcmPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(wcmPol))

    // CA7
    var ca7Line = Mockito.mock(productmodel.CA7Line, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(ca7Line.Subtype).thenReturn(typekey.PolicyLine.TC_CA7COMMAUTOLINE)
    Mockito.when(ca7Line.CA7VehicleLiab.CA7LimitTextTerm.DisplayValue).thenReturn("2,000,000")
    var ca7Pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(ca7Pol.Lines).thenReturn(new entity.PolicyLine[]{ca7Line})
    Mockito.when(ca7Pol.CA7Line).thenReturn(ca7Line)
    assertEquals("2.000", EpdsApiHelper.getLimits(ca7Pol))

    // CUL
    var culLine = Mockito.mock(productmodel.CommercialUmbrellaLine)
    Mockito.when(culLine.Subtype).thenReturn(typekey.PolicyLine.TC_CULCOMMERCIALUMBRELLALINE)
    Mockito.when(culLine.formatCULComUmbCoverageGeneralAggregateCustomLimit()).thenReturn("5,000,000")
    var culPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(culPol.Lines).thenReturn(new entity.PolicyLine[]{culLine})
    Mockito.when(culPol.CommercialUmbrellaLine).thenReturn(culLine)
    assertEquals("5.000", EpdsApiHelper.getLimits(culPol))

    // WPL Casualty & Energy (null subproduct default)
    var wplLine = Mockito.mock(productmodel.PrimaryCasualtyLine)
    Mockito.when(wplLine.Subtype).thenReturn(typekey.PolicyLine.TC_WPLPRIMARYCASUALTYLINE)
    var wplPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(wplPol.Lines).thenReturn(new entity.PolicyLine[]{wplLine})
    Mockito.when(wplPol.PrimaryCasualtyLine.SubProduct).thenReturn(null)
    assertEquals("0.000", EpdsApiHelper.getLimits(wplPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(wplPol))

    // WEL Casualty & Energy
    var welLine = Mockito.mock(productmodel.ExcessCasualtyLine, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welLine.Subtype).thenReturn(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE)
    Mockito.when(welLine.ExcessSubProduct).thenReturn(WELSubProduct.TC_EXCESSCASUALTY)
    Mockito.when(welLine.WELExcessCasualty.WELExcessCasualtyAggLimAppTerm.DisplayValue).thenReturn("3,000,000")
    Mockito.when(welLine.WELExcessCasualty.WELExcessCasualtyNonAutoAttachTerm.DisplayValue).thenReturn("500,000")

    var welCasPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welCasPol.Lines).thenReturn(new entity.PolicyLine[]{welLine})
    Mockito.when(welCasPol.ExcessCasualtyLineExists).thenReturn(true)
    Mockito.when(welCasPol.ExcessCasualtyLine).thenReturn(welLine)
    assertEquals("3.000", EpdsApiHelper.getLimits(welCasPol))
    assertEquals("0.500", EpdsApiHelper.getAttachmentPoint(welCasPol))

    var welEnergyLine = Mockito.mock(productmodel.ExcessCasualtyLine, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welEnergyLine.Subtype).thenReturn(typekey.PolicyLine.TC_WELEXCESSCASUALTYLINE)
    Mockito.when(welEnergyLine.ExcessSubProduct).thenReturn(WELSubProduct.TC_EXCESSENERGY)
    Mockito.when(welEnergyLine.WELGeneralLiability.WELGeneralLiabilityAggLimitsTerm.DisplayValue).thenReturn("4,000,000")
    Mockito.when(welEnergyLine.WELGeneralLiability.WELGeneralLiabilityAttachmentPointTerm.DisplayValue).thenReturn("1,000,000")

    var welEnergyPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(welEnergyPol.Lines).thenReturn(new entity.PolicyLine[]{welEnergyLine})
    Mockito.when(welEnergyPol.ExcessCasualtyLineExists).thenReturn(true)
    Mockito.when(welEnergyPol.ExcessCasualtyLine).thenReturn(welEnergyLine)
    assertEquals("4.000", EpdsApiHelper.getLimits(welEnergyPol))
    assertEquals("1.000", EpdsApiHelper.getAttachmentPoint(welEnergyPol))

    // Default
    var defaultLine = Mockito.mock(productmodel.GL7Line)
    Mockito.when(defaultLine.Subtype).thenReturn(null)
    var defaultPol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(defaultPol.Lines).thenReturn(new entity.PolicyLine[]{defaultLine})
    assertEquals("0.000", EpdsApiHelper.getLimits(defaultPol))
    assertEquals("0.000", EpdsApiHelper.getAttachmentPoint(defaultPol))
  }

  function testMapEpdsPartySearchRequest() {
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"
    criteria.Country = Country.TC_US

    var request = new EpdsApiHelper().mapEpdsPartySearchRequest(criteria)

    assertNotNull(request.GlobalTrackingId)
    assertEquals("Ford", request.OrgName)
    assertEquals(Country.TC_US as String, request.Country)
  }

  function testEpdsPartySearchReturnsNullOnFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"

    var response = new FailingEpdsApiHelper(feignException).epdsPartySearch(criteria)
    assertNull(response)
  }

  function testEpdsPartySearchReturnsNullOnRuntimeException() {
    var criteria = new AccountSearchCriteria()
    criteria.CompanyName = "Ford"

    var response = new FailingEpdsApiHelper(new RuntimeException("Service down")).epdsPartySearch(criteria)
    assertNull(response)
  }

  function testEpdsContractDataSyncWhenIneligible() {
    var pol = Mockito.mock(PolicyPeriod, Mockito.RETURNS_DEEP_STUBS)
    Mockito.when(pol.Status).thenReturn(PolicyPeriodStatus.TC_BOUND)
    var helper = new EpdsApiHelper()

    helper.epdsContractDataSync(pol)
    // When ineligible, call is not initiated
    assertFalse(EpdsApiHelper.isContractDataSyncCallEligible(pol))
  }

  // ===================================================================
  // 4. EpdsApiClient - epdsSearchPartyData
  // ===================================================================

  function testEpdsSearchPartyDataReturnsResponse() {
    var expected = new PartyDataSearchResponse()
    var client = new StubEpdsApiClient(expected, null)

    var actual = client.epdsSearchPartyData(new PartyDataSearchRequest())

    assertEquals(expected, actual)
    assertEquals(CommonConstants.EPDS_SEARCH_PARTY, client.retrieveServiceName())
  }

  function testEpdsSearchPartyDataReturnsNullForUnexpectedResponse() {
    assertNull(new StubEpdsApiClient(null, null).epdsSearchPartyData(new PartyDataSearchRequest()))
    assertNull(new StubEpdsApiClient("WRONG_TYPE", null).epdsSearchPartyData(new PartyDataSearchRequest()))
  }

  function testEpdsSearchPartyDataRethrowsFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var client = new StubEpdsApiClient(null, feignException)

    var caught = false
    try {
      client.epdsSearchPartyData(new PartyDataSearchRequest())
    } catch (e : FeignException) {
      caught = true
    }
    assertTrue(caught)
  }

  function testEpdsSearchPartyDataRethrowsRuntimeException() {
    var client = new StubEpdsApiClient(null, new RuntimeException("Service error"))

    var caught = false
    try {
      client.epdsSearchPartyData(new PartyDataSearchRequest())
    } catch (e : RuntimeException) {
      caught = true
    }
    assertTrue(caught)
  }

  // ===================================================================
  // 5. EpdsApiClient - epdsContractDataSync
  // ===================================================================

  function testEpdsContractDataSyncExecutesSuccessfully() {
    var client = new StubEpdsApiClient(null, null)

    client.epdsContractDataSync(new ContractDataSyncRequest())

    assertEquals(CommonConstants.EPDS_CONTRACT_DATA_SYNC, client.retrieveServiceName())
  }

  function testEpdsContractDataSyncRethrowsFeignException() {
    var feignException = Mockito.mock(FeignException)
    Mockito.when(feignException.getStackTrace()).thenReturn(new StackTraceElement[0])
    var client = new StubEpdsApiClient(null, feignException)

    var caught = false
    try {
      client.epdsContractDataSync(new ContractDataSyncRequest())
    } catch (e : FeignException) {
      caught = true
    }
    assertTrue(caught)
  }

  function testEpdsContractDataSyncRethrowsRuntimeException() {
    var client = new StubEpdsApiClient(null, new RuntimeException("Sync error"))

    var caught = false
    try {
      client.epdsContractDataSync(new ContractDataSyncRequest())
    } catch (e : RuntimeException) {
      caught = true
    }
    assertTrue(caught)
  }

  // ===================================================================
  // 6. EpdsApiClient - doServiceCall & retrieveServiceName
  // ===================================================================

  function testEpdsApiClientRetrieveServiceNameInitialState() {
    var client = new EpdsApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))
    assertNull(client.retrieveServiceName())
  }

  function testEpdsApiClientDoServiceCallAllBranches() {
    var client = new EpdsApiClient(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))

    assertNull(client.doServiceCall(null, "UNSUPPORTED_REQUEST"))
    assertNull(client.doServiceCall(null, null))

    var searchReq = new PartyDataSearchRequest()
    var searchResp = new PartyDataSearchResponse()
    var syncReq = new ContractDataSyncRequest()
    var syncResp = new ContractDataSyncTransformResponse()

    var mockApi = Mockito.mock(EpdsApi)
    Mockito.when(mockApi.igPartyDataSearchAPI(searchReq)).thenReturn(searchResp)
    Mockito.when(mockApi.igContractDataSyncAPI(syncReq)).thenReturn(syncResp)

    var mockConfig = Mockito.mock(Config)
    Mockito.when(mockConfig.buildAPI(EpdsApi)).thenReturn(mockApi)

    assertEquals(searchResp, client.doServiceCall(mockConfig, searchReq))
    assertEquals(syncResp, client.doServiceCall(mockConfig, syncReq))
  }

  // ===================================================================
  // Test doubles (small nested classes, no logic other than return/throw)
  // ===================================================================

  static class StubEpdsApiClient extends EpdsApiClient {
    var _response : Object
    var _exception : Exception

    construct(response : Object, exception : Exception) {
      super(new RuntimePropertyRetriever(RuntimePropertyGroup.TC_EPDSNAMECLEARANCE_EXT))
      _response = response
      _exception = exception
    }

    override function doGet(request : Object) : Object {
      if (_exception != null) {
        throw _exception
      }
      return _response
    }

    override function doPost(request : Object) : Object {
      if (_exception != null) {
        throw _exception
      }
      return _response
    }
  }

  static class FailingEpdsApiHelper extends EpdsApiHelper {
    var _exception : Exception

    construct(exception : Exception) {
      _exception = exception
    }

    override function invokeEpdsPartySearch(request : PartyDataSearchRequest) : PartyDataSearchResponse {
      throw _exception
    }

    override function invokeEpdsContractDataSync(request : ContractDataSyncRequest) {
      throw _exception
    }
  }

}
