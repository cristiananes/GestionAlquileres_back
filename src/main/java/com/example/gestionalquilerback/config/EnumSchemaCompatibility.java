package com.example.gestionalquilerback.config;
import com.example.gestionalquilerback.model.enums.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Component;
import javax.sql.DataSource;import java.util.*;import java.util.regex.*;
/** Append-only compatibility: retain enum order, nullability, default and collation. */
@Component @Order(Ordered.HIGHEST_PRECEDENCE) @RequiredArgsConstructor
public class EnumSchemaCompatibility implements ApplicationRunner {
 private static final Logger log=LoggerFactory.getLogger(EnumSchemaCompatibility.class);
 private final DataSource dataSource;private final JdbcTemplate jdbc;
 @Override public void run(ApplicationArguments args) throws Exception {
  try(var connection=dataSource.getConnection()) {
   var product=connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
   if(!product.contains("mysql")&&!product.contains("tidb"))return;
  }
  var income=Arrays.stream(IncomeType.values()).map(Enum::name).toList();var expense=Arrays.stream(ExpenseCategory.values()).map(Enum::name).toList();
  expand("income","income_type",income);expand("expense","category",expense);expand("recurring_rule","income_type",income);expand("recurring_rule","category",expense);
 }
 private void expand(String table,String column,List<String> required) {
  var rows=jdbc.queryForList("select COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, CHARACTER_SET_NAME, COLLATION_NAME from information_schema.columns where TABLE_SCHEMA=database() and TABLE_NAME=? and COLUMN_NAME=? and DATA_TYPE='enum'",table,column);
  if(rows.isEmpty())return;
  var row=rows.getFirst();var type=appendMembers(String.valueOf(row.get("COLUMN_TYPE")),required);if(type==null)return;
  String charset=identifier(row.get("CHARACTER_SET_NAME")),collation=identifier(row.get("COLLATION_NAME"));
  String suffix=(charset==null?"":" CHARACTER SET "+charset)+(collation==null?"":" COLLATE "+collation)+(String.valueOf(row.get("IS_NULLABLE")).equalsIgnoreCase("YES")?" NULL":" NOT NULL");
  var defaultValue=row.get("COLUMN_DEFAULT");
  if(defaultValue!=null) {String value=String.valueOf(defaultValue);if(!value.matches("[A-Z][A-Z0-9_]*"))throw new IllegalStateException("Unsupported default for "+table+"."+column);suffix+=" DEFAULT '"+value+"'";}
  jdbc.execute("ALTER TABLE "+table+" MODIFY COLUMN "+column+" "+type+suffix);log.info("Updated enum compatibility for {}.{}",table,column);
 }
 private static String identifier(Object value) {if(value==null)return null;String s=String.valueOf(value);if(!s.matches("[a-zA-Z0-9_]+"))throw new IllegalStateException("Unsupported enum character set/collation");return s;}
 public static String appendMembers(String current,List<String> required) {
  if(current==null||!current.matches("enum\\('[A-Z][A-Z0-9_]*'(?:,'[A-Z][A-Z0-9_]*')*\\)"))throw new IllegalStateException("Unsupported enum definition; refusing to reorder or replace its values");
  var values=new ArrayList<String>();var matcher=Pattern.compile("'([A-Z][A-Z0-9_]*)'").matcher(current);while(matcher.find())values.add(matcher.group(1));int original=values.size();
  for(String member:required) {if(!member.matches("[A-Z][A-Z0-9_]*"))throw new IllegalArgumentException("Invalid enum member");if(!values.contains(member))values.add(member);}
  return original==values.size()?null:"enum('"+String.join("','",values)+"')";
 }
}
