SELECT
    COALESCE(d.docuid, d.documentidentifier) AS "documentId",
    d.docuid AS "docUID",
    d.documentidentifier AS "documentIdentifier",
    pp.policynumber AS "policyNumber",
    bu.name AS "OBU",
    COALESCE(
        c.name,
        NULLIF(TRIM(COALESCE(c.firstname, '') || ' ' || COALESCE(c.lastname, '')), '')
    ) AS "accountName",
    a.accountnumber AS "accountNumber",
    dt.name AS "docType",
    COALESCE(d.datecreated, d.createtime) AS "createDate",
    d.datemodified AS "modifyDate",
    d.updatetime AS "updateDate",
    p.productcode AS "lineOfBusiness",
    pp.periodstart AS "inceptionDate",
    pp.periodstart AS "effectiveDate",
    pp.periodend AS "expirationDate",
    CASE
        WHEN d.jobid IS NOT NULL OR d.policyperiodid IS NOT NULL THEN 'Transaction Level'
        WHEN d.policyid IS NOT NULL THEN 'Policy Level'
        WHEN d.accountid IS NOT NULL THEN 'Account Level'
        ELSE 'Unlinked'
    END AS "documentLevel",
    CASE
        WHEN d.jobid IS NOT NULL THEN COALESCE(jt.name, 'Transaction') || ' : ' || COALESCE(j.jobnumber, '')
        WHEN d.policyperiodid IS NOT NULL THEN 'PolicyPeriod : ' || COALESCE(pp.policynumber, '')
        WHEN d.policyid IS NOT NULL THEN 'Policy : ' || COALESCE(pp.policynumber, '')
        WHEN d.accountid IS NOT NULL THEN 'Account : ' || COALESCE(a.accountnumber, '')
        ELSE NULL
    END AS "relatedTo"
FROM pc_document d
LEFT JOIN pctl_documenttype dt
    ON dt.id = d.type
LEFT JOIN pc_job j
    ON j.id = d.jobid
   AND j.retired = 0
LEFT JOIN pctl_job jt
    ON jt.id = j.subtype
LEFT JOIN pc_jobpolicyperiod jpp
    ON jpp.ownerid = j.id
LEFT JOIN pc_policyperiod pp
    ON pp.id = COALESCE(d.policyperiodid, jpp.foreignentityid)
   AND pp.retired = 0
LEFT JOIN pctl_businessunit_ext bu
    ON bu.id = pp.businessunit_ext
LEFT JOIN pc_policy p
    ON p.id = COALESCE(d.policyid, pp.policyid)
   AND p.retired = 0
LEFT JOIN pc_account a
    ON a.id = COALESCE(d.accountid, p.accountid)
   AND a.retired = 0
LEFT JOIN pc_acctholderedge ahe
    ON ahe.ownerid = a.id
LEFT JOIN pc_accountcontact ac
    ON ac.id = ahe.foreignentityid
   AND ac.retired = 0
LEFT JOIN pc_contact c
    ON c.id = ac.contactid
   AND c.retired = 0
WHERE d.retired = 0
  AND (
      d.docuid IN (
          /* Paste your documentId UUIDs here */
          '7b5fe627-64f9-4188-b4dc-7f555c0f9596',
          '3e81acfc-42d9-4978-8ecb-45c4184290f1',
          '9fbb170d-a7b4-48a6-9770-f2704a593daf',
          'f2475675-c3ff-4fe7-b540-73b73ff89b0e',
          'f4785b2a-8e8b-446c-b553-e7ea2677c7d0',
          '2bddb9c0-9caa-4274-8a9b-5ea26636925a'
      )
      OR d.documentidentifier IN (
          /* Paste the same documentId UUIDs here */
          '7b5fe627-64f9-4188-b4dc-7f555c0f9596',
          '3e81acfc-42d9-4978-8ecb-45c4184290f1',
          '9fbb170d-a7b4-48a6-9770-f2704a593daf',
          'f2475675-c3ff-4fe7-b540-73b73ff89b0e',
          'f4785b2a-8e8b-446c-b553-e7ea2677c7d0',
          '2bddb9c0-9caa-4274-8a9b-5ea26636925a'
      )
  )

