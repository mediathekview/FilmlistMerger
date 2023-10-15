package de.mediathekview.fimlistmerger.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Table;

@Service
@Transactional
public class FilmlistMerge {
  Logger LOG = LoggerFactory.getLogger(FilmlistMerge.class);
    
  @Autowired
  EntityManager entityManager;

  
  public void removeOldEntries() {
    String sql = createMergeQuery().toString();
    LOG.info("geneated sql " + sql);
    //try { Thread.sleep(10*1000); } catch(Exception e) {}
    javax.persistence.Query query = entityManager.createNativeQuery(sql);
    int rs = query.executeUpdate();
    LOG.info("executed sql rs: " + rs);
  }
  
  private StringBuilder createMergeQuery() {
    StringBuilder sql = new StringBuilder();
    String tablename = "T" + System.currentTimeMillis();
    sql
      .append("create table ").append(tablename).append("(id uuid, insert_date timestamp);");
    sql  
      .append("insert into ")
      .append(tablename)
      .append(" select uuid, insert_timestamp from (")
        .append(" select uuid, insert_timestamp, row_number() over (partition by sender, thema, titel, duration order by insert_timestamp desc) rn")
        .append(" from ").append(Film.class.getAnnotation(Table.class).name())
        .append(") oldEntries")
        .append(" where rn > 1")
      .append(";");
    sql
      .append("delete from ").append("film_geo_locations")
      .append(" where film_uuid in (select id from ").append(tablename).append(")")
      .append(";");
    sql
      .append("delete from ").append("film_subtitles")
      .append(" where film_uuid in (select id from ").append(tablename).append(")")
      .append(";");
    sql
      .append("delete from ").append("film_url")
      .append(" where film_uuid in (select id from ").append(tablename).append(")")
      .append(";");
    sql
      .append("delete from ").append(Film.class.getAnnotation(Table.class).name())
      .append(" where uuid in (select id from ").append(tablename).append(")")
      .append(";");
    sql
      .append("drop table ").append(tablename).append(";");
    return sql;
  }
  
  /*
   * create table temp_mergeIds (id uuid, insert_date timestamp);
insert into temp_mergeIds
select uuid, insert_timestamp from (
  select uuid, insert_timestamp, row_number() over (partition by sender, thema, titel, duration order by insert_timestamp desc) rn from film 
) x where rn > 1;
delete from film_geo_locations where film_uuid in (select id from temp_mergeIds);
delete from film_subtitles where film_uuid in (select id from temp_mergeIds);
delete from film_url where film_id in (select id from temp_mergeIds);
delete from film where uuid in (select id from temp_mergeIds);
   */
  
  
  
}
