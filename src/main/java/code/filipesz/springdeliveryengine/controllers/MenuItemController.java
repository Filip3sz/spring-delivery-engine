package code.filipesz.springdeliveryengine.controllers;

import code.filipesz.springdeliveryengine.dto.MenuItemRequest;
import code.filipesz.springdeliveryengine.entities.MenuItem;
import code.filipesz.springdeliveryengine.services.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menuitems")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public ResponseEntity<List<MenuItem>> menuList() {
        return ResponseEntity.ok(menuItemService.menuList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getItemById(id));
    }

    @PostMapping
    public ResponseEntity<MenuItem> addItem(@Valid @RequestBody MenuItemRequest request) {
        return new ResponseEntity<>(menuItemService.addItem(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> editItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.editItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuItemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}