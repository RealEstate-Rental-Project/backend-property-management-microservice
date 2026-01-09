package com.lsiproject.app.propertymanagementmicroservice.services;

import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationRequestDTO;
import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.DTOs.UserManagementDto;
import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.PropertyResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.UpdateDTOs.PropertyUpdateDTO;
import com.lsiproject.app.propertymanagementmicroservice.contract.RealEstateRental;
import com.lsiproject.app.propertymanagementmicroservice.CreationDTOs.PropertyCreationDTO;
import com.lsiproject.app.propertymanagementmicroservice.entities.Property;
import com.lsiproject.app.propertymanagementmicroservice.mappers.PropertyMapper;
import com.lsiproject.app.propertymanagementmicroservice.openFeignClients.PropertyRecommendationModel;
import com.lsiproject.app.propertymanagementmicroservice.openFeignClients.UserManagementMicroService;
import com.lsiproject.app.propertymanagementmicroservice.repository.PropertyRepository;
import com.lsiproject.app.propertymanagementmicroservice.searchDTOs.PropertySearchDTO;
import com.lsiproject.app.propertymanagementmicroservice.security.UserPrincipal;
import com.lsiproject.app.propertymanagementmicroservice.wrappers.PropertyRecommendationResponseWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service to manage property CRUD operations, synchronizing off-chain data (MySQL)
 * with on-chain data (RealEstateRental contract).
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final RealEstateRental rentalContract;
    private final SupabaseStorageService storageService;
    private final RoomService roomService;
    private final UserManagementMicroService userManagementClient;
    private final PropertyRecommendationModel recommendationClient;
    private final PropertyMapper propertyMapper;


    public PropertyService(
            PropertyRepository propertyRepository,
            Web3j web3j,
            Credentials credentials,
            StaticGasProvider gasProvider,
            RoomService roomService,
            @Value("${contract.rental.address}") String contractAddress,
            SupabaseStorageService storageService,
            UserManagementMicroService userManagementClient,
            PropertyRecommendationModel recommendationClient,
            PropertyMapper propertyMapper

    ) {
        this.propertyRepository = propertyRepository;
        this.roomService = roomService;

        // Load the deployed contract wrapper
        this.rentalContract = RealEstateRental.load(
                contractAddress, web3j, credentials, gasProvider
        );
        this.storageService = storageService;

        this.userManagementClient = userManagementClient;
        this.recommendationClient = recommendationClient;
        this.propertyMapper = propertyMapper;
    }

    /**
     * Searches for properties based on dynamic criteria (City, Rent, Type, Location).
     * @param searchDTO Object containing filter parameters.
     * @return List of matching properties.
     */
    public List<Property> searchProperties(PropertySearchDTO searchDTO) {
        // Use the specification to build the query dynamically
        return propertyRepository.findAll(
                PropertySpecification.getPropertiesByCriteria(searchDTO)
        );
    }

    /**
     * Creates a new property off-chain, lists it on-chain, and uploads rooms/images to Supabase.
     * @param dto The property details, including nested rooms/images.
     * @param ownerId The ID of the owner (from JWT claims).
     * @param ownerEthAddress The wallet address of the owner (from JWT claims).
     * @return The saved Property entity.
     * @throws Exception if the blockchain transaction fails.
     */
    @Transactional
    public Property createProperty(
            PropertyCreationDTO dto,
            Long ownerId,
            String ownerEthAddress
    ) throws Exception {


        Property property = new Property();
        property.setOnChainId(dto.onChainId());
        property.setTitle(dto.title());
        property.setCountry(dto.country());
        property.setCity(dto.city());
        property.setAddress(dto.address());
        property.setLongitude(dto.longitude());
        property.setLatitude(dto.latitude());
        property.setDescription(dto.description());
        property.setSqM(dto.sqM());
        property.setTypeOfProperty(dto.typeOfProperty());
        property.setRentAmount(dto.rentAmount());
        property.setSecurityDeposit(dto.securityDeposit());
        property.setTypeOfRental(dto.typeOfRental());
        property.setOwnerId(ownerId);
        property.setOwnerEthAddress(ownerEthAddress);
        property.setIsActive(true);
        property.setIsAvailable(true);

        return propertyRepository.save(property);
    }


    /**
     * Updates a property off-chain and synchronously updates the details on-chain.
     * @param id The database ID (idProperty).
     * @param dto The property entity with new data.
     * @param currentOwnerEthAddress The wallet address of the caller (for on-chain authorization).
     * @return The updated Property entity.
     * @throws Exception if the blockchain transaction fails or property not found.
     */
    @Transactional
    public Property updateProperty(Long id, PropertyUpdateDTO dto, String currentOwnerEthAddress) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found in database."));

        // Ensure the caller is authorized (off-chain check)
        if (!property.getOwnerEthAddress().equalsIgnoreCase(currentOwnerEthAddress)) {
            throw new SecurityException("Caller is not the property owner.");
        }

        // Ensure onChainId is present before updating
        Long onChainId = property.getOnChainId();
        if (onChainId == null) {
            throw new IllegalStateException("Property is not yet listed on chain.");
        }

        // 2. Update Off-Chain Data
        if (dto.title() != null) {
            property.setTitle(dto.title());
        }
        if (dto.country() != null) {
            property.setCountry(dto.country());
        }
        if (dto.city() != null) {
            property.setCity(dto.city());
        }
        if (dto.address() != null) {
            property.setAddress(dto.address());
        }
        if (dto.description() != null) {
            property.setDescription(dto.description());
        }
        if (dto.typeOfProperty() != null) {
            property.setTypeOfProperty(dto.typeOfProperty());
        }
        if (dto.SqM() != null) {
            property.setSqM(dto.SqM());
        }
        if (dto.total_Rooms() != null) {
            property.setTotal_Rooms(dto.total_Rooms());
        }
        if (dto.rentAmount() != null) {
            property.setRentAmount(dto.rentAmount());
        }
        if (dto.securityDeposit() != null) {
            property.setSecurityDeposit(dto.securityDeposit());
        }
        if (dto.typeOfRental() != null) {
            property.setTypeOfRental(dto.typeOfRental());
        }
        if (dto.isAvailable() != null) {
            property.setIsAvailable(dto.isAvailable());
        }


        property.setUpdatedAt(LocalDateTime.now());
        return propertyRepository.save(property);
    }

    public void updateAvailabilityToFalse(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        property.setIsAvailable(false);
        propertyRepository.save(property);
    }

    public void updateAvailabilityToTrue(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        property.setIsAvailable(true);
        propertyRepository.save(property);
    }

    public TypeOfRental getTypeOfRental(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return property.getTypeOfRental();
    }


    /**
     * Deletes a property by delisting it on-chain and marking it inactive in the database.
     * @param id The database ID (idProperty).
     * @param currentOwnerEthAddress The wallet address of the caller.
     * @throws Exception if the blockchain transaction fails or property not found.
     */
    @Transactional
    public void deleteProperty(Long id, String currentOwnerEthAddress) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found in database."));

        // Ensure the caller is authorized (off-chain check)
        if (!property.getOwnerEthAddress().equalsIgnoreCase(currentOwnerEthAddress)) {
            throw new SecurityException("Caller is not the property owner.");
        }

        // 2. Update Off-Chain Data
        property.setIsActive(false);
        property.setIsAvailable(false);
        property.setUpdatedAt(LocalDateTime.now());
        propertyRepository.save(property);
    }


    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public boolean isPropertyAvailable(Long id){
        return propertyRepository.existsByIdPropertyAndIsAvailableTrue(id);
    }

    /**
     * Récupère les 3 propriétés les plus récentes(just pour le moment, apres on vas utiliser un AI system pour ca) qui sont actives et disponibles.
     */
    public List<Property> getMostRecentProperties() {
        return propertyRepository.findTop3ByIsActiveTrueAndIsAvailableTrueOrderByCreatedAtDesc();
    }

    public List<Property> getPropertiesByOwnerId(Long ownerId) {
        return propertyRepository.findAllByOwnerId(ownerId);
    }

    /**
     * Retrieves a single property by ID and performs an on-chain status check
     * for verification. (This replaces the previous getPropertyById and getProperty).
     */
    public Property getProperty(Long id) throws Exception {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Property not found."));


        return property;
    }


    public List<PropertyResponseDTO> getRecommendedProperties(UserPrincipal principal) {
        // 1. Get User profile from UserManagement service
        UserManagementDto userProfile;
        try {
            userProfile = userManagementClient.getUserById(principal.getIdUser());
        } catch (Exception e) {
            System.err.println("Failed to fetch user profile: " + e.getMessage());
            return new ArrayList<>(); // Or return getMostRecentProperties() converted to DTOs
        }

        if (userProfile == null) {
            return new ArrayList<>();
        }

        // 2. Map User profile to AI Request DTO
        PropertyRecommendationRequestDTO request = new PropertyRecommendationRequestDTO(
                userProfile.getTargetRent() != null ? BigDecimal.valueOf(userProfile.getTargetRent()) : BigDecimal.ZERO,
                userProfile.getMinTotalRooms(),
                userProfile.getTargetSqft(),
                userProfile.getSearchLatitude(),
                userProfile.getSearchLongitude(),
                userProfile.getPreferredPropertyType(),
                userProfile.getPreferredRentalType(),
                1, // Default user_id placeholder if needed by Python model
                false
        );

        System.out.println("Request sent to the AI model: " + request);

        // 3. Get recommended property IDs from AI Service (Using Wrapper)
        List<PropertyRecommendationResponseDTO> aiResponses;
        try {
            // CHANGED: Use the wrapper to handle {"recommendations": [...]}
            PropertyRecommendationResponseWrapper wrapper = recommendationClient.recommend_properties(request);

            if (wrapper == null || wrapper.getRecommendations() == null || wrapper.getRecommendations().isEmpty()) {
                System.out.println("AI Model returned no recommendations.");
                return new ArrayList<>();
            }
            aiResponses = wrapper.getRecommendations();

        } catch (Exception e) {
            System.err.println("Recommendation AI Service failed: " + e.getMessage());
            return new ArrayList<>();
        }


        // 4. Extract IDs
        List<Long> propertyIds = aiResponses.stream()
                .map(PropertyRecommendationResponseDTO::getProperty_id)
                .collect(Collectors.toList());

        // 5. Fetch properties from DB
        List<Property> properties = propertyRepository.findAllByIdPropertyIn(propertyIds);

        System.out.println(properties.toString());

        // 6. Map to Response DTOs using your PropertyMapper
        return properties.stream()
                .map(propertyMapper::toDto)
                .collect(Collectors.toList());
    }

}