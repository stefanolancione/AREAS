package sianc.sisar.corso;


import java.util.List;

import PSGExt.profile.CustomQueryProfile;
import PSGLibrary.DB.QueryException;
import PSGLibrary.DB.SQLUtils;
import PSGLibrary.Persistence.JDBCDataMapper;

public class CorsoQuery extends CustomQueryProfile {

  public CorsoQuery() {
    formalFilters = "codeFilter,descriptionFilter,teacherFilter,statusFilter";
    fieldsName = "CR_CODE,CR_DESCRIPTION, DECODE(CR_END_DATE, NULL,'Attivo','Disattivo') CR_STATO_CORSO";
    fieldsCaption = "Codice,Descrizione,Stato del Corso"; // use tilde to hide a column
  }

  @Override
  protected String getCustomSqlString() throws QueryException {
    String sql = "SELECT " + getFieldsName() + " FROM SA_SARD_CORSO WHERE 1 = 1 ";

    // Check if the user set this filter
    if (isFilterPresent("codeFilter")) {
      // Take the filter value
      Object filterValue = getFilterValue("codeFilter");
      // Build the SQL - JDBCDataMapper.objectToSQL : sanitizzazione dell'input (evito la sql injection)
      sql += " AND CR_CODE = " + JDBCDataMapper.objectToSQL(filterValue);
    }

    if (isFilterPresent("descriptionFilter")) {
      //sql += " AND " + likeStatement("CR_DESCRIPTION", "descriptionFilter");
      sql += addAND(likeStatement("CR_DESCRIPTION","descriptionFilter"));
    }

    if (isFilterPresent("teacherFilter")) {
      //sql += " AND " + likeStatement("CR_TEACHER", "teacherFilter");
      sql += addAND(likeStatement("CR_TEACHER","teacherFilter"));
    }

    if(isFilterPresent("statusFilter")) {
      Object statusFilterValue = getFilterValue("statusFilter");

      if ("A".equals(statusFilterValue)) {
        sql += " AND CR_END_DATE IS NULL";
      } else if ("D".equals(statusFilterValue)){
        sql += " AND CR_END_DATE IS NOT NULL";
      }
    }


    // SUPPORTO FIND3 IN RICERCA CORSO : filtro listaStudenti
    if (isFilterPresent("listaStudenti")) {
      List filterValue = (List)getFilterValue("listaStudenti");

      sql += " AND CR_CODE IN (SELECT CS_CORSO FROM SA_SARD_CORSO_STUDENTE WHERE CS_STUDENTE IN (" +  SQLUtils.collection2commaSeparatedString(filterValue) + "))";
    }

    return sql;
  }

}