package service;

import dto.CartItemRequest;
import dto.ProductRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import model.CartItem;
import model.Product;
import model.User;
import org.springframework.stereotype.Service;
import repository.CartItemRepository;
import repository.ProductRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService{

    private final ProductRepository productRepository;
    private final CartItemRequest cartItemRequest;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public Boolean addToCart(String userId, CartItemRequest request) {
        Optional<Product> productOpt = productRepository.findById(Long.valueOf(request.getProductId()));
        if(productOpt.isEmpty())
            return false;

        Product product = productOpt.get();
        if(product.getStockQuantity() < request.getQuantity())
            return false;

        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        if(userOpt.isEmpty())
            return false;

        User user = userOpt.get();
        CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user, product);
        if(existingCartItem != null){
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
            existingCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
            cartItemRepository.save(existingCartItem);
        }else{
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(cartItem);
        }

        return true;
    }

    public boolean deleteItemFromCart(User user, Product product) {
        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product);

        if (cartItem != null){
            cartItemRepository.delete(cartItem);
            return true;
        }
        return false;
    }

    public List<CartItem> getCart(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}