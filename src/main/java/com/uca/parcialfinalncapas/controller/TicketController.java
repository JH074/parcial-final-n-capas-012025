package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponseList;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.repository.TicketRepository;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {
    private TicketService ticketService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','TECH')")
    public ResponseEntity<GeneralResponse> getAllTickets() {

        List<TicketResponseList> all = ticketService.getAllTickets();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTech = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECH"));

        List<TicketResponseList> filtered = all;
        if (!isTech) {

            String correo = auth.getName();
            Long userId = userRepository.findByCorreo(correo)
                    .orElseThrow(() -> new BadTicketRequestException("Usuario no encontrado"))
                    .getId();

            filtered = all.stream()
                    .filter(t -> t.getSolicitanteId().equals(userId))
                    .toList();
        }

        return ResponseBuilderUtil.buildResponse(
                "Tickets obtenidos correctamente",
                filtered.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK,
                filtered
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','TECH')")
    public ResponseEntity<GeneralResponse> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTech = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TECH"));

        if (!isTech && !ticket.getCorreoSolicitante().equals(auth.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("No puedes ver este ticket");
        }

        return ResponseBuilderUtil.buildResponse("Ticket encontrado", HttpStatus.OK, ticket);
    }


    @PostMapping
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<GeneralResponse> createTicket(@Valid @RequestBody TicketCreateRequest ticket) {
        TicketResponse createdTicket = ticketService.createTicket(ticket);
        return ResponseBuilderUtil.buildResponse("Ticket creado correctamente", HttpStatus.CREATED, createdTicket);
    }

    @PutMapping
    @PreAuthorize("hasRole('TECH')")

    public ResponseEntity<GeneralResponse> updateTicket(@Valid @RequestBody TicketUpdateRequest ticket) {
        TicketResponse updatedTicket = ticketService.updateTicket(ticket);
        return ResponseBuilderUtil.buildResponse("Ticket actualizado correctamente", HttpStatus.OK, updatedTicket);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TECH')")

    public ResponseEntity<GeneralResponse> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseBuilderUtil.buildResponse("Ticket eliminado correctamente", HttpStatus.OK, null);
    }
}
