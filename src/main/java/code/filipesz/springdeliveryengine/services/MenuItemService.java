package code.filipesz.springdeliveryengine.services;

import code.filipesz.springdeliveryengine.dto.MenuItemRequest;
import code.filipesz.springdeliveryengine.entities.MenuItem;
import code.filipesz.springdeliveryengine.repositories.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItem> menuList() {
        return menuItemRepository.findAll();
    }

    public MenuItem getItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pozycji o takim id."));
    }

    @Transactional
    public MenuItem addItem(MenuItemRequest request) {
        MenuItem menuItem = MenuItem.builder()
                .name(request.name())
                .price(request.price())
                .build();

        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public MenuItem editItem(Long id, MenuItemRequest request) {
        MenuItem product = getItemById(id);

        product.setName(request.name());
        product.setPrice(request.price());

        return product;
    }

    @Transactional
    public void deleteItem(Long id) {
        MenuItem menuItem = getItemById(id);
        menuItemRepository.delete(menuItem);
    }
}