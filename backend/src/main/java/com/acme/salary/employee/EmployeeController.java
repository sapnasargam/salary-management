package com.acme.salary.employee;

import java.net.URI;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.salary.common.FieldValidationException;
import com.acme.salary.common.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EmployeeResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort order = EmployeeSortField.fromApiName(sort).toSort(parseDirection(direction));
        PageRequest pageRequest = PageRequest.of(page, size, order);
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria(search, country, department, jobTitle);
        return PageResponse.from(service.search(criteria, pageRequest));
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable long id, @Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static Sort.Direction parseDirection(String direction) {
        return Sort.Direction.fromOptionalString(direction)
                .orElseThrow(() -> FieldValidationException.invalid("direction", "must be 'asc' or 'desc'"));
    }
}
