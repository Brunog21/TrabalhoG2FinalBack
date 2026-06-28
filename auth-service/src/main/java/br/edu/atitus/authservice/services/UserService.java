package br.edu.atitus.authservice.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.authservice.components.Validator;
import br.edu.atitus.authservice.dtos.UpdateProfileDTO;
import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
    public static final double DEFAULT_BALANCE = 1800.0;

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        super();
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    private void validate(UserEntity user) throws Exception {
        if (user.getName() == null || user.getName().isEmpty())
            throw new Exception("Nome informado inválido");
        if (user.getEmail() == null || user.getEmail().isEmpty() || !Validator.validateEmail(user.getEmail()))
            throw new Exception("E-mail informado inválido");
        if (user.getPassword() == null || user.getPassword().isEmpty())
            throw new Exception("Senha informada inválida");

        if (user.getId() != null) {
            if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId()))
                throw new Exception("Já existe usuário com este e-mail");
        } else {
            if (userRepository.existsByEmail(user.getEmail()))
                throw new Exception("Já existe usuário com este e-mail");
        }
        // TODO validar se usuário tem permissão para o tipo escolhido
    }

    private void format(UserEntity user) throws Exception {
        user.setPassword(encoder.encode(user.getPassword()));
    }

    private void applyDefaults(UserEntity user) {
        if (user.getBalance() == null) {
            user.setBalance(DEFAULT_BALANCE);
        }
        if (user.getStreet() == null) {
            user.setStreet("");
        }
        if (user.getNumber() == null) {
            user.setNumber("");
        }
        if (user.getComplement() == null) {
            user.setComplement("");
        }
        if (user.getNeighborhood() == null) {
            user.setNeighborhood("");
        }
        if (user.getCity() == null) {
            user.setCity("");
        }
        if (user.getState() == null) {
            user.setState("");
        }
        if (user.getZipCode() == null) {
            user.setZipCode("");
        }
    }

    @Transactional
    public UserEntity save(UserEntity user) throws Exception {
        if (user == null)
            throw new Exception("Objeto nulo");
        applyDefaults(user);
        validate(user);
        format(user);
        return userRepository.save(user);
    }

    public UserEntity findById(Long id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new Exception("Usuário não encontrado"));
    }

    @Transactional
    public UserEntity updateProfile(Long userId, UpdateProfileDTO dto) throws Exception {
        var user = findById(userId);

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name().trim());
        }

        if (dto.avatarUrl() != null) {
            String avatarUrl = dto.avatarUrl().trim();
            user.setAvatarUrl(avatarUrl.isEmpty() ? null : avatarUrl);
        }

        if (dto.street() != null) {
            user.setStreet(dto.street().trim());
        }
        if (dto.number() != null) {
            user.setNumber(dto.number().trim());
        }
        if (dto.complement() != null) {
            user.setComplement(dto.complement().trim());
        }
        if (dto.neighborhood() != null) {
            user.setNeighborhood(dto.neighborhood().trim());
        }
        if (dto.city() != null) {
            user.setCity(dto.city().trim());
        }
        if (dto.state() != null) {
            user.setState(dto.state().trim().toUpperCase());
        }
        if (dto.zipCode() != null) {
            user.setZipCode(dto.zipCode().trim());
        }

        return userRepository.save(user);
    }

    @Transactional
    public UserEntity debit(Long userId, double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Valor inválido para débito");
        }

        var user = findById(userId);
        double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;

        if (currentBalance < amount) {
            throw new Exception("Sem saldo suficiente para realizar compra");
        }

        user.setBalance(Math.round((currentBalance - amount) * 100.0) / 100.0);
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity credit(Long userId, double amount) throws Exception {
        if (amount <= 0) {
            throw new Exception("Valor inválido para crédito");
        }

        var user = findById(userId);
        double currentBalance = user.getBalance() != null ? user.getBalance() : 0.0;
        user.setBalance(Math.round((currentBalance + amount) * 100.0) / 100.0);
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com este e-mail"));
        return user;
    }
}
