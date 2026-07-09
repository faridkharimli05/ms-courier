package az.delivery.mscourier.controller;

import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.enums.CourierStatus;
import az.delivery.mscourier.exception.GlobalExceptionHandler;
import az.delivery.mscourier.service.CourierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourierControllerTest {

    private CourierService courierService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courierService = mock(CourierService.class);
        CourierController controller = new CourierController(courierService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAvailableCourierReturnsFreeCourier() throws Exception {
        when(courierService.getAvailableCourier())
                .thenReturn(new CourierResponseDto(1L, "Farid", "+994501112233", CourierStatus.FREE, null, List.of()));

        mockMvc.perform(get("/api/couriers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("FREE"));
    }

    @Test
    void getCourierOrderHistoryReturnsRecordedOrders() throws Exception {
        when(courierService.getCourierOrderHistory(1L)).thenReturn(List.of(10L, 15L));

        mockMvc.perform(get("/api/couriers/1/history"))
                .andExpect(status().isOk())
                .andExpect(content().json("[10,15]"));
    }
}
