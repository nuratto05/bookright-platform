package com.bookrights.controller;

import com.bookrights.model.CustomUserDetails;
import com.bookrights.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class OrderControllerTest {

    private UsernamePasswordAuthenticationToken authenticatedPrincipal() {
        com.bookrights.model.User user = new com.bookrights.model.User(
                "buyer", "Buyer", "buyer@test.local", "password");
        user.setId(1L);
        return new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(user), null, List.of(new SimpleGrantedAuthority("USER")));
    }

    private MockMvc mvcFor(OrderService service) {
        return MockMvcBuilders.standaloneSetup(new OrderController(service))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    void placeOrderMapsInsufficientBalanceToBadRequest() throws Exception {
        OrderService service = Mockito.mock(OrderService.class);
        MockMvc mvc = mvcFor(service);
        when(service.createOrder(any(), any()))
        .thenThrow(new IllegalArgumentException("Insufficient balance in buyer's wallet"));

        mvc.perform(MockMvcRequestBuilders.post("/transaction/order/placeOrder")
                        .principal(authenticatedPrincipal())
                        .contentType("application/json")
                        .content("{\"bookId\":10}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void acceptMapsInvalidStatusToBadRequest() throws Exception {
        OrderService service = Mockito.mock(OrderService.class);
        MockMvc mvc = mvcFor(service);
        when(service.acceptOrder(any(), any()))
        .thenThrow(new IllegalStateException("Invalid Order Status"));

        mvc.perform(MockMvcRequestBuilders.put("/transaction/order/30/accept").principal(authenticatedPrincipal()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid Order Status"));
    }

    @Test
    void rejectWithMismatchStatusMessageCurrentlyReturnsInternalServerError() throws Exception {
        OrderService service = mock(OrderService.class);
        MockMvc mvc = mvcFor(service);
        when(service.rejectOrder(any(), any()))
        .thenThrow(new IllegalStateException("Only pending orders can be rejected"));

        mvc.perform(MockMvcRequestBuilders.put("/transaction/order/30/reject").principal(authenticatedPrincipal()))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.content()
                .string("An unexpected error occurred : Only pending orders can be rejected"));
    }

    @Test
    void cancelWithMismatchStatusMessageCurrentlyReturnsBadRequest() throws Exception {
        OrderService service = mock(OrderService.class);
        MockMvc mvc = mvcFor(service);
        when(service.cancelOrder(any(), eq(30L)))
        .thenThrow(new IllegalStateException("Only pending orders can be cancelled"));

        mvc.perform(MockMvcRequestBuilders.put("/transaction/order/30/cancel").principal(authenticatedPrincipal()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Only pending orders can be cancelled"));
    }
}
