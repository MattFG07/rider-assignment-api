
package com.softer.riderassignment;

import com.softer.riderassignment.model.AssignmentStatus;
import com.softer.riderassignment.model.RiderAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderAssignmentTest {

    private static final String BASE_PATH = "/api/assignments";
    private static final String CLEAN_SQL = "DELETE FROM rider_assignments;";

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(1)
    @DisplayName("should register a new assignment")
    @Sql(statements = CLEAN_SQL, executionPhase = AFTER_TEST_METHOD)
    void createAssignment() {
        RiderAssignment payload = new RiderAssignment(null, 7001L, 301L, AssignmentStatus.ASSIGNED, null);

        ResponseEntity<RiderAssignment> response = restTemplate.postForEntity(
                BASE_PATH, payload, RiderAssignment.class);

        RiderAssignment body = response.getBody();
        assertAll(
                () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
                () -> assertNotNull(body),
                () -> assertNotNull(body.getId()),
                () -> assertEquals(7001L, body.getDeliveryId()),
                () -> assertEquals(301L, body.getRiderId()),
                () -> assertEquals(AssignmentStatus.ASSIGNED, body.getStatus()),
                () -> assertNotNull(body.getAssignedAt())
        );
    }

    @Test
    @Order(2)
    @DisplayName("should return empty list when table is empty")
    void getAllEmpty() {
        ResponseEntity<RiderAssignment[]> response = restTemplate.getForEntity(
                BASE_PATH, RiderAssignment[].class);

        RiderAssignment[] body = response.getBody();
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(body),
                () -> assertEquals(0, body.length)
        );
    }

    @Test
    @Order(3)
    @DisplayName("should return one assignment when one record exists")
    @SqlGroup({
            @Sql(
                    statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) VALUES (10, 7010, 310, 'ASSIGNED', CURRENT_TIMESTAMP);",
                    executionPhase = BEFORE_TEST_METHOD
            ),
            @Sql(statements = CLEAN_SQL, executionPhase = AFTER_TEST_METHOD)
    })
    void getAllWithOneRecord() {
        ResponseEntity<RiderAssignment[]> response = restTemplate.getForEntity(
                BASE_PATH, RiderAssignment[].class);

        RiderAssignment[] body = response.getBody();
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(body),
                () -> assertEquals(1, body.length),
                () -> assertEquals(10L, body[0].getId()),
                () -> assertEquals(7010L, body[0].getDeliveryId()),
                () -> assertEquals(310L, body[0].getRiderId()),
                () -> assertEquals(AssignmentStatus.ASSIGNED, body[0].getStatus())
        );
    }

    @Test
    @Order(4)
    @DisplayName("should retrieve assignment by id")
    @SqlGroup({
            @Sql(
                    statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) VALUES (20, 7020, 320, 'ACCEPTED', CURRENT_TIMESTAMP);",
                    executionPhase = BEFORE_TEST_METHOD
            ),
            @Sql(statements = CLEAN_SQL, executionPhase = AFTER_TEST_METHOD)
    })
    void getById() {
        ResponseEntity<RiderAssignment> response = restTemplate.getForEntity(
                BASE_PATH + "/20", RiderAssignment.class);

        RiderAssignment body = response.getBody();
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(body),
                () -> assertEquals(20L, body.getId()),
                () -> assertEquals(7020L, body.getDeliveryId()),
                () -> assertEquals(320L, body.getRiderId()),
                () -> assertEquals(AssignmentStatus.ACCEPTED, body.getStatus())
        );
    }

    @Test
    @Order(5)
    @DisplayName("should update assignment by id")
    @SqlGroup({
            @Sql(
                    statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) VALUES (30, 7030, 330, 'ASSIGNED', CURRENT_TIMESTAMP);",
                    executionPhase = BEFORE_TEST_METHOD
            ),
            @Sql(statements = CLEAN_SQL, executionPhase = AFTER_TEST_METHOD)
    })
    void updateById() {
        RiderAssignment updatePayload = new RiderAssignment(null, 7030L, 399L, AssignmentStatus.DELIVERED, null);

        ResponseEntity<RiderAssignment> response = restTemplate.exchange(
                BASE_PATH + "/30",
                HttpMethod.PUT,
                new HttpEntity<>(updatePayload),
                RiderAssignment.class
        );

        RiderAssignment body = response.getBody();
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(body),
                () -> assertEquals(30L, body.getId()),
                () -> assertEquals(7030L, body.getDeliveryId()),
                () -> assertEquals(399L, body.getRiderId()),
                () -> assertEquals(AssignmentStatus.DELIVERED, body.getStatus())
        );
    }

    @Test
    @Order(6)
    @DisplayName("should delete assignment by id")
    @SqlGroup({
            @Sql(
                    statements = "INSERT INTO rider_assignments (id, delivery_id, rider_id, status, assigned_at) VALUES (40, 7040, 340, 'CANCELLED', CURRENT_TIMESTAMP);",
                    executionPhase = BEFORE_TEST_METHOD
            ),
            @Sql(statements = CLEAN_SQL, executionPhase = AFTER_TEST_METHOD)
    })
    void deleteById() {
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_PATH + "/40",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        ResponseEntity<String> getAfterDelete = restTemplate.getForEntity(
                BASE_PATH + "/40", String.class);

        assertAll(
                () -> assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode()),
                () -> assertEquals(HttpStatus.NOT_FOUND, getAfterDelete.getStatusCode())
        );
    }
}