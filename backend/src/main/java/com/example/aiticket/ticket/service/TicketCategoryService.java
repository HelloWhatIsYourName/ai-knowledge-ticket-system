package com.example.aiticket.ticket.service;

import com.example.aiticket.ticket.domain.TicketCategory;
import com.example.aiticket.ticket.mapper.TicketMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketCategoryService {
    private final TicketMapper mapper;

    public TicketCategoryService(TicketMapper mapper) {
        this.mapper = mapper;
    }

    public List<TicketCategory> list(boolean includeDisabled) {
        return mapper.listTicketCategories(includeDisabled);
    }

    @Transactional
    public TicketCategory create(String name, Long parentId, Integer sortOrder, Boolean enabled) {
        String normalizedName = normalizedName(name);
        int normalizedSortOrder = sortOrder == null ? 0 : sortOrder;
        boolean normalizedEnabled = enabled == null || enabled;
        Long id = mapper.nextTicketCategoryId();
        mapper.insertTicketCategory(id, normalizedName, parentId, normalizedSortOrder, normalizedEnabled);
        return category(id, normalizedName, parentId, normalizedSortOrder, normalizedEnabled);
    }

    @Transactional
    public TicketCategory update(Long id, String name, Long parentId, Integer sortOrder) {
        String normalizedName = normalizedName(name);
        int normalizedSortOrder = sortOrder == null ? 0 : sortOrder;
        mapper.updateTicketCategory(id, normalizedName, parentId, normalizedSortOrder);
        return category(id, normalizedName, parentId, normalizedSortOrder, true);
    }

    @Transactional
    public TicketCategory enable(Long id) {
        mapper.updateTicketCategoryEnabled(id, true);
        return currentOrFallback(id, true);
    }

    @Transactional
    public TicketCategory disable(Long id) {
        mapper.updateTicketCategoryEnabled(id, false);
        return currentOrFallback(id, false);
    }

    private TicketCategory currentOrFallback(Long id, boolean enabled) {
        return mapper.listTicketCategories(true).stream()
                .filter(category -> category.id().equals(id))
                .findFirst()
                .map(category -> new TicketCategory(category.id(), category.name(), category.parentId(),
                        category.sortOrder(), enabled, category.createdAt(), LocalDateTime.now()))
                .orElseGet(() -> category(id, "未命名分类", null, 0, enabled));
    }

    private TicketCategory category(Long id, String name, Long parentId, int sortOrder, boolean enabled) {
        LocalDateTime now = LocalDateTime.now();
        return new TicketCategory(id, name, parentId, sortOrder, enabled, now, now);
    }

    private String normalizedName(String name) {
        if (name == null || name.isBlank()) {
            throw new TicketWorkflowException("ticket category name is required");
        }
        return name.trim();
    }
}
