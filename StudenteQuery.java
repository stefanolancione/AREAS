package sianc.sisar.studente;

import PSGExt.profile.CustomQueryProfile;
import PSGLibrary.DB.QueryException;
import PSGLibrary.Persistence.JDBCDataMapper;


public class StudenteQuery extends CustomQueryProfile {


  public StudenteQuery() {
    formalFilters = "idFilter,nomeFilter,cognomeFilter,cittadinanzaFilter";
    fieldsName = "ST_ID,ST_NAME || ' ' || ST_SURNAME";
    fieldsCaption = "Matricola,Nome Completo"; // use tilde to hide a column
  }


  @Override
  protected String getCustomSqlString() throws QueryException {

    String sql = "SELECT " + getFieldsName() + " FROM SA_SARD_STUDENTE WHERE 1 = 1 ";

    // Check if the user set this filter
    if (isFilterPresent("idFilter")) {
      // Take the filter value
      Object filterValue = getFilterValue("idFilter");
      // Build the SQL - JDBCDataMapper.objectToSQL : sanitizzazione dell'input (evito la sql injection)
      sql += " AND ST_ID = " + JDBCDataMapper.objectToSQL(filterValue);
    }

    //nome e cognome : like
    if (isFilterPresent("nomeFilter")) {
      // Build the SQL - JDBCDataMapper.objectToSQL : sanitizzazione dell'input (evito la sql injection)
      sql += addAND( likeStatement("ST_NAME", "nomeFilter") );
    }
    if (isFilterPresent("cognomeFilter")) {
      // Build the SQL - JDBCDataMapper.objectToSQL : sanitizzazione dell'input (evito la sql injection)
      sql += addAND( likeStatement("ST_SURNAME", "cognomeFilter") );
    }
    if (isFilterPresent("cittadinanzaFilter")) {
      // Take the filter value
      Object filterValue = getFilterValue("cittadinanzaFilter");
      System.out.println("Filtro CittadinanzaFilter = " + filterValue);
      // Build the SQL - JDBCDataMapper.objectToSQL : sanitizzazione dell'input (evito la sql injection)
      sql += " AND ST_CITTADINANZA = " + JDBCDataMapper.objectToSQL(filterValue);
    }

    //filtro aggiuntio per gestione FIND2 su studente aperta da form associazione corso-studente
    if (isFilterPresent("soloIscritti")) {
      Object filterValue = getFilterValue("soloIscritti");
      sql += " AND ST_START_DATE <= " + JDBCDataMapper.objectToSQL(filterValue);
      sql += " AND NVL(ST_END_DATE,SYSDATE) >= " + JDBCDataMapper.objectToSQL(filterValue);
    }

    return sql;

  }

}
