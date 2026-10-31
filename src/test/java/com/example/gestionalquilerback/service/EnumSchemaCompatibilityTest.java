package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.config.EnumSchemaCompatibility;
import org.junit.jupiter.api.Test;import java.util.List;import static org.junit.jupiter.api.Assertions.*;
class EnumSchemaCompatibilityTest {
 @Test void appendKeepsExistingOrdinalPositionsAndUnrecognisedLegacyMembers() {
  assertEquals("enum('RENT','DEPOSIT','OTHER','LEGACY','UTILITY_REIMBURSEMENT')",EnumSchemaCompatibility.appendMembers("enum('RENT','DEPOSIT','OTHER','LEGACY')",List.of("DEPOSIT","OTHER","RENT","UTILITY_REIMBURSEMENT")));
 }
 @Test void repeatStartupIsANoOp() {assertNull(EnumSchemaCompatibility.appendMembers("enum('RENT','UTILITY_REIMBURSEMENT')",List.of("UTILITY_REIMBURSEMENT","RENT")));}
 @Test void invalidDefinitionsNeverProduceDdl() {assertThrows(IllegalStateException.class,()->EnumSchemaCompatibility.appendMembers("enum('RENT'); DROP TABLE income",List.of("OTHER")));assertThrows(IllegalArgumentException.class,()->EnumSchemaCompatibility.appendMembers("enum('RENT')",List.of("OTHER'); DROP TABLE income")));}
}
