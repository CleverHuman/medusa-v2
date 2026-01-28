// Global variables
let currentStep = 1;
const totalSteps = 4;
let formData = {};
let marketExperienceEntryCounter = 0;

// Declare global functions early for inline handlers
window.addMarketExperienceEntry = function(entry = {}, shouldFocus = true) {
    // Will be called after DOM is loaded
    if (typeof addMarketExperienceEntry_internal === 'function') {
        addMarketExperienceEntry_internal(entry, shouldFocus);
    } else {
        console.error('addMarketExperienceEntry_internal not yet initialized');
    }
};

window.toggleMarketExperience = function() {
    if (typeof toggleMarketExperience_internal === 'function') {
        toggleMarketExperience_internal();
    } else {
        console.error('toggleMarketExperience_internal not yet initialized');
    }
};

// Initialize after page load
document.addEventListener('DOMContentLoaded', function() {
    initializeForm();
    loadDraft();
    setupMarketExperienceSection();
    setupEventListeners();
});

// Initialize form
function initializeForm() {
    updateStepIndicators();
    updateProgressBar();
    showStep(1);
}

// Setup event listeners
function setupEventListeners() {
    // Form submit event
    document.getElementById('applicationForm').addEventListener('submit', handleFormSubmit);
    
    // Real-time validation
    setupRealTimeValidation();
    
    // Contact method selection events
    setupContactMethods();
}

// Setup real-time validation
function setupRealTimeValidation() {
    // Vendor name validation
    const vendorName = document.getElementById('vendorName');
    if (vendorName) {
        vendorName.addEventListener('blur', function() {
            validateField('vendorName', this.value, 'Vendor name is required');
        });
    }

    // PGP public key validation
    const pgpSignature = document.getElementById('pgpSignature');
    if (pgpSignature) {
        pgpSignature.addEventListener('blur', function() {
            validatePGPKey('pgpSignature', this.value);
        });
    }

    // Email validation
    const emailFields = ['emailAddress', 'secondaryEmailAddress'];
    emailFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field) {
            field.addEventListener('blur', function() {
                validateEmail(fieldId, this.value);
            });
        }
    });
}

// Setup contact methods selection
function setupContactMethods() {
    // Primary contact methods
    const primaryCheckboxes = document.querySelectorAll('input[name="primaryContacts"]');
    primaryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateContactField(this, 'primary');
            validateContactMethods('primaryContacts', 'Please select at least one primary contact method');
        });
    });

    // Secondary contact methods
    const secondaryCheckboxes = document.querySelectorAll('input[name="secondaryContacts"]');
    secondaryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            updateContactField(this, 'secondary');
            validateContactMethods('secondaryContacts', 'Please select at least one secondary contact method');
        });
    });
}

// Update contact field status
function updateContactField(checkbox, type) {
    const value = checkbox.value;
    const fieldId = type === 'primary' ? 
        (value === 'telegram' ? 'telegramId' : 
         value === 'signal' ? 'signalId' : 
         value === 'jabber' ? 'jabberId' : 'emailAddress') :
        (value === 'telegram' ? 'secondaryTelegramId' : 
         value === 'signal' ? 'secondarySignalId' : 
         value === 'jabber' ? 'secondaryJabberId' : 'secondaryEmailAddress');
    
    const field = document.getElementById(fieldId);
    if (field) {
        field.disabled = !checkbox.checked;
        if (checkbox.checked) {
            field.focus();
        } else {
            field.value = '';
        }
    }
}

// Toggle market experience section
function toggleMarketExperience_internal() {
    const select = document.getElementById('hasMarketExperience');
    const section = document.getElementById('marketExperienceSection');
    const hiddenInput = document.getElementById('marketExperienceData');
    const container = document.getElementById('marketExperienceEntries');
    
    if (select && section) {
        if (select.value === 'yes') {
            section.classList.remove('hidden');
            if (typeof anime !== 'undefined') {
                anime({
                    targets: section,
                    opacity: [0, 1],
                    translateY: [-20, 0],
                    duration: 300,
                    easing: 'easeOutQuad'
                });
            }
            
            if (container && container.querySelectorAll('[data-entry-id]').length === 0) {
                addMarketExperienceEntry_internal();
            }
            
            updateMarketExperienceData();
        } else {
            if (typeof anime !== 'undefined') {
                anime({
                    targets: section,
                    opacity: [1, 0],
                    translateY: [0, -20],
                    duration: 300,
                    easing: 'easeOutQuad',
                    complete: function() {
                        section.classList.add('hidden');
                    }
                });
            } else {
                section.classList.add('hidden');
            }
            
            if (hiddenInput) {
                hiddenInput.value = '[]';
            }
            
            showValidationMessage('marketExperienceData', '', 'success');
        }
    }
}

// Setup market experience section
function setupMarketExperienceSection() {
    const select = document.getElementById('hasMarketExperience');
    const container = document.getElementById('marketExperienceEntries');
    const hiddenInput = document.getElementById('marketExperienceData');
    const section = document.getElementById('marketExperienceSection');
    
    if (!select || !container || !hiddenInput || !section) {
        console.error('Market experience elements not found', {select, container, hiddenInput, section});
        return;
    }
    
    console.log('Setting up market experience section...');
    
    // Default to "yes"
    if (!select.value) {
        select.value = 'yes';
    }
    
    // Ensure section is visible initially if value is "yes"
    if (select.value === 'yes') {
        section.classList.remove('hidden');
        section.style.opacity = '1';
    }
    
    // Restore saved entries if available
    const storedRaw = formData.existingMarkets || hiddenInput.value;
    const storedEntries = parseMarketExperienceData(storedRaw);
    
    console.log('Rendering market experience entries:', storedEntries);
    renderMarketExperienceEntries(storedEntries.length > 0 ? storedEntries : [{}]);
    
    // Register container listeners to sync data
    container.addEventListener('input', updateMarketExperienceData);
    container.addEventListener('change', updateMarketExperienceData);
    
    // Recalculate hidden data
    updateMarketExperienceData();
    
    // React to dropdown changes
    select.addEventListener('change', function() {
        console.log('Market experience dropdown changed to:', select.value);
        toggleMarketExperience_internal();
        updateMarketExperienceData();
    });
    
    console.log('Market experience section setup complete');
}

// Render market experience entries
function renderMarketExperienceEntries(entries = []) {
    const container = document.getElementById('marketExperienceEntries');
    if (!container) return;
    
    container.innerHTML = '';
    entries.forEach(entry => addMarketExperienceEntry_internal(entry, false));
    
    if (container.querySelectorAll('[data-entry-id]').length === 0) {
        addMarketExperienceEntry_internal();
    }
}

// Add market experience entry
function addMarketExperienceEntry_internal(entry = {}, shouldFocus = true) {
    console.log('addMarketExperienceEntry_internal called with:', entry, 'shouldFocus:', shouldFocus);
    const container = document.getElementById('marketExperienceEntries');
    if (!container) {
        console.error('Market experience entries container not found!');
        return;
    }
    
    console.log('Container found, creating entry...');
    const entryId = `market-entry-${marketExperienceEntryCounter++}`;
    const wrapper = document.createElement('div');
    wrapper.className = 'border border-gray-200 rounded-xl p-4 bg-white shadow-sm relative';
    wrapper.dataset.entryId = entryId;
    
    const grid = document.createElement('div');
    grid.className = 'grid md:grid-cols-3 gap-4';
    
    const currentYear = new Date().getFullYear();
    
    // Market name field
    const marketNameGroup = document.createElement('div');
    const marketNameLabel = document.createElement('label');
    marketNameLabel.className = 'block text-sm font-medium text-gray-700 mb-2';
    marketNameLabel.textContent = 'Market Name *';
    const marketNameInput = document.createElement('input');
    marketNameInput.type = 'text';
    marketNameInput.dataset.field = 'marketName';
    marketNameInput.className = 'input-field w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent';
    marketNameInput.placeholder = 'e.g., XYZ Market';
    marketNameInput.value = entry.marketName || '';
    marketNameGroup.appendChild(marketNameLabel);
    marketNameGroup.appendChild(marketNameInput);
    
    // Start year field
    const startYearGroup = document.createElement('div');
    const startYearLabel = document.createElement('label');
    startYearLabel.className = 'block text-sm font-medium text-gray-700 mb-2';
    startYearLabel.textContent = 'From Year *';
    const startYearInput = document.createElement('input');
    startYearInput.type = 'number';
    startYearInput.dataset.field = 'startYear';
    startYearInput.min = '1990';
    startYearInput.max = String(currentYear + 5);
    startYearInput.className = 'input-field w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent';
    startYearInput.placeholder = 'Start year';
    startYearInput.value = entry.startYear || '';
    startYearGroup.appendChild(startYearLabel);
    startYearGroup.appendChild(startYearInput);
    
    // End year field
    const endYearGroup = document.createElement('div');
    const endYearLabel = document.createElement('label');
    endYearLabel.className = 'block text-sm font-medium text-gray-700 mb-2';
    endYearLabel.textContent = 'To Year';
    const endYearInput = document.createElement('input');
    endYearInput.type = 'number';
    endYearInput.dataset.field = 'endYear';
    endYearInput.min = '1990';
    endYearInput.max = String(currentYear + 5);
    endYearInput.className = 'input-field w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent';
    endYearInput.placeholder = 'End year (leave blank if ongoing)';
    endYearInput.value = entry.endYear || '';
    endYearGroup.appendChild(endYearLabel);
    endYearGroup.appendChild(endYearInput);
    
    grid.appendChild(marketNameGroup);
    grid.appendChild(startYearGroup);
    grid.appendChild(endYearGroup);
    wrapper.appendChild(grid);
    
    // Remove button
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.className = 'mt-4 inline-flex items-center px-3 py-2 text-sm font-medium text-red-600 hover:text-red-700';
    removeButton.textContent = 'Remove';
    removeButton.addEventListener('click', function() {
        removeMarketExperienceEntry(entryId);
    });
    wrapper.appendChild(removeButton);
    
    container.appendChild(wrapper);
    console.log('Market entry added successfully with ID:', entryId);
    
    if (shouldFocus && marketNameInput) {
        marketNameInput.focus();
    }
    
    updateMarketExperienceData();
}

console.log('addMarketExperienceEntry_internal defined');

// Remove market experience entry
function removeMarketExperienceEntry(entryId) {
    const container = document.getElementById('marketExperienceEntries');
    if (!container) return;
    
    const entry = container.querySelector(`[data-entry-id="${entryId}"]`);
    if (entry) {
        container.removeChild(entry);
    }
    
    if (container.querySelectorAll('[data-entry-id]').length === 0) {
        addMarketExperienceEntry_internal({}, false);
    }
    
    updateMarketExperienceData();
}

// Collect market experience entries from DOM
function collectMarketExperienceEntries() {
    const container = document.getElementById('marketExperienceEntries');
    if (!container) return [];
    
    const entries = [];
    container.querySelectorAll('[data-entry-id]').forEach(entryEl => {
        const marketNameInput = entryEl.querySelector('input[data-field="marketName"]');
        const startYearInput = entryEl.querySelector('input[data-field="startYear"]');
        const endYearInput = entryEl.querySelector('input[data-field="endYear"]');
        
        entries.push({
            marketName: marketNameInput ? marketNameInput.value.trim() : '',
            startYear: startYearInput ? startYearInput.value.trim() : '',
            endYear: endYearInput ? endYearInput.value.trim() : ''
        });
    });
    
    return entries;
}

// Update hidden market experience data
function updateMarketExperienceData() {
    const hiddenInput = document.getElementById('marketExperienceData');
    const select = document.getElementById('hasMarketExperience');
    if (!hiddenInput || !select) return;
    
    if (select.value !== 'yes') {
        hiddenInput.value = '[]';
        return;
    }
    
    const rawEntries = collectMarketExperienceEntries();
    const filteredEntries = rawEntries.filter(entry => entry.marketName || entry.startYear || entry.endYear);
    
    hiddenInput.value = JSON.stringify(filteredEntries);
}

// Parse stored market experience data
function parseMarketExperienceData(raw) {
    if (!raw) {
        return [];
    }
    
    try {
        const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        console.warn('Failed to parse market experience data:', error);
        return [];
    }
}

// Calculate total experience years from entries
function calculateExperienceYears(entries = []) {
    if (!Array.isArray(entries) || entries.length === 0) {
        return null;
    }
    
    const currentYear = new Date().getFullYear();
    let earliest = Number.POSITIVE_INFINITY;
    let latest = Number.NEGATIVE_INFINITY;
    
    entries.forEach(entry => {
        const startYear = parseInt(entry.startYear, 10);
        if (!Number.isNaN(startYear)) {
            earliest = Math.min(earliest, startYear);
        }
        
        const endYearValue = entry.endYear ? parseInt(entry.endYear, 10) : currentYear;
        if (!Number.isNaN(endYearValue)) {
            latest = Math.max(latest, endYearValue);
        }
    });
    
    if (!Number.isFinite(earliest) || !Number.isFinite(latest) || latest < earliest) {
        return null;
    }
    
    return latest - earliest + 1;
}

// Validate market experience entries
function validateMarketExperienceEntries() {
    const select = document.getElementById('hasMarketExperience');
    if (!select || select.value !== 'yes') {
        showValidationMessage('marketExperienceData', '', 'success');
        return true;
    }
    
    const entries = parseMarketExperienceData(document.getElementById('marketExperienceData')?.value || '[]');
    const currentYear = new Date().getFullYear() + 5;
    
    if (entries.length === 0) {
        showValidationMessage('marketExperienceData', 'Please add at least one market experience entry with required fields filled in', 'error');
        return false;
    }
    
    for (const entry of entries) {
        if (!entry.marketName || !entry.startYear) {
            showValidationMessage('marketExperienceData', 'Market name and start year are required', 'error');
            return false;
        }
        
        const startYearNum = parseInt(entry.startYear, 10);
        if (Number.isNaN(startYearNum) || startYearNum < 1990 || startYearNum > currentYear) {
            showValidationMessage('marketExperienceData', 'Enter a valid start year between 1990 and the current year', 'error');
            return false;
        }
        
        if (entry.endYear) {
            const endYearNum = parseInt(entry.endYear, 10);
            if (Number.isNaN(endYearNum) || endYearNum < startYearNum || endYearNum > currentYear) {
                showValidationMessage('marketExperienceData', 'Enter a valid end year that is not earlier than the start year', 'error');
                return false;
            }
        }
    }
    
    showValidationMessage('marketExperienceData', '', 'success');
    return true;
}

// Change step
function changeStep(direction) {
    const newStep = currentStep + direction;
    
    if (newStep < 1 || newStep > totalSteps) {
        return;
    }

    // Validate current step
    if (direction > 0 && !validateCurrentStep()) {
        return;
    }

    // Save current step data
    saveCurrentStepData();

    // Switch step
    if (direction > 0) {
        nextStep();
    } else {
        prevStep();
    }
}

// Next step
function nextStep() {
    if (currentStep < totalSteps) {
        hideStep(currentStep);
        currentStep++;
        showStep(currentStep);
        updateStepIndicators();
        updateProgressBar();
        updateNavigationButtons();
        
        if (currentStep === 4) {
            generateSummary();
        }
    }
}

// Previous step
function prevStep() {
    if (currentStep > 1) {
        hideStep(currentStep);
        currentStep--;
        showStep(currentStep);
        updateStepIndicators();
        updateProgressBar();
        updateNavigationButtons();
    }
}

// Show specified step
function showStep(step) {
    const stepElement = document.getElementById(`step${step}`);
    if (stepElement) {
        stepElement.classList.add('active');
        anime({
            targets: stepElement,
            opacity: [0, 1],
            translateX: [50, 0],
            duration: 400,
            easing: 'easeOutQuad'
        });
    }
}

// Hide specified step
function hideStep(step) {
    const stepElement = document.getElementById(`step${step}`);
    if (stepElement) {
        anime({
            targets: stepElement,
            opacity: [1, 0],
            translateX: [0, -50],
            duration: 300,
            easing: 'easeOutQuad',
            complete: function() {
                stepElement.classList.remove('active');
            }
        });
    }
}

// Update step indicators
function updateStepIndicators() {
    for (let i = 1; i <= totalSteps; i++) {
        const indicator = document.querySelector(`[data-step="${i}"]`);
        if (indicator) {
            indicator.classList.remove('active', 'completed');
            
            if (i === currentStep) {
                indicator.classList.add('active');
            } else if (i < currentStep) {
                indicator.classList.add('completed');
            }
        }
    }
}

// Update progress bar
function updateProgressBar() {
    const progressBar = document.querySelector('.progress-bar');
    if (progressBar) {
        const progress = (currentStep / totalSteps) * 100;
        progressBar.style.width = `${progress}%`;
    }
}

// Update navigation buttons
function updateNavigationButtons() {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');

    if (prevBtn) {
        prevBtn.style.display = currentStep > 1 ? 'block' : 'none';
    }

    if (nextBtn && submitBtn) {
        if (currentStep === totalSteps) {
            nextBtn.style.display = 'none';
            submitBtn.style.display = 'block';
        } else {
            nextBtn.style.display = 'block';
            submitBtn.style.display = 'none';
        }
    }
}

// Validate current step
function validateCurrentStep() {
    switch (currentStep) {
        case 1:
            return validateStep1();
        case 2:
            return validateStep2();
        case 3:
            return validateStep3();
        default:
            return true;
    }
}

// Validate step 1
function validateStep1() {
    let isValid = true;

    // Validate vendor name
    const vendorName = document.getElementById('vendorName');
    if (!validateField('vendorName', vendorName.value, 'Vendor name is required')) {
        isValid = false;
    }

    // Validate PGP public key
    const pgpSignature = document.getElementById('pgpSignature');
    if (!validatePGPKey('pgpSignature', pgpSignature.value)) {
        isValid = false;
    }

    // Validate location
    const location = document.getElementById('location');
    if (!validateField('location', location.value, 'Please select your location')) {
        isValid = false;
    }

    if (!validateMarketExperienceEntries()) {
        isValid = false;
    }

    return isValid;
}

// Validate step 2
function validateStep2() {
    let isValid = true;

    // Validate product categories
    const productCategories = document.querySelectorAll('input[name="productCategories"]:checked');
    if (productCategories.length === 0) {
        showValidationMessage('productCategories', 'Please select at least one product category', 'error');
        isValid = false;
    } else {
        showValidationMessage('productCategories', '', 'success');
    }

    // Validate product description
    const productDescription = document.getElementById('productDescription');
    if (!validateField('productDescription', productDescription.value, 'Please describe the products you are likely to sell')) {
        isValid = false;
    }

    return isValid;
}

// Validate step 3
function validateStep3() {
    let isValid = true;

    // Validate primary contact methods
    const primaryContacts = document.querySelectorAll('input[name="primaryContacts"]:checked');
    if (primaryContacts.length === 0) {
        showValidationMessage('primaryContacts', 'Please select at least one primary contact method', 'error');
        isValid = false;
    } else {
        // Validate if selected contact methods have details filled in
        let hasValidPrimaryContact = false;
        primaryContacts.forEach(contact => {
            const value = contact.value;
            const fieldId = value === 'telegram' ? 'telegramId' : 
                           value === 'signal' ? 'signalId' : 
                           value === 'jabber' ? 'jabberId' : 'emailAddress';
            const field = document.getElementById(fieldId);
            if (field && field.value.trim() !== '') {
                hasValidPrimaryContact = true;
            }
        });
        
        if (!hasValidPrimaryContact) {
            showValidationMessage('primaryContacts', 'Please fill in primary contact details', 'error');
            isValid = false;
        } else {
            showValidationMessage('primaryContacts', '', 'success');
        }
    }

    // Validate secondary contact methods
    const secondaryContacts = document.querySelectorAll('input[name="secondaryContacts"]:checked');
    if (secondaryContacts.length === 0) {
        showValidationMessage('secondaryContacts', 'Please select at least one secondary contact method', 'error');
        isValid = false;
    } else {
        // Validate if selected contact methods have details filled in
        let hasValidSecondaryContact = false;
        secondaryContacts.forEach(contact => {
            const value = contact.value;
            const fieldId = value === 'telegram' ? 'secondaryTelegramId' : 
                           value === 'signal' ? 'secondarySignalId' : 
                           value === 'jabber' ? 'secondaryJabberId' : 'secondaryEmailAddress';
            const field = document.getElementById(fieldId);
            if (field && field.value.trim() !== '') {
                hasValidSecondaryContact = true;
            }
        });
        
        if (!hasValidSecondaryContact) {
            showValidationMessage('secondaryContacts', 'Please fill in secondary contact details', 'error');
            isValid = false;
        } else {
            showValidationMessage('secondaryContacts', '', 'success');
        }
    }

    return isValid;
}

// Validate field
function validateField(fieldId, value, errorMessage) {
    if (!value || value.trim() === '') {
        showValidationMessage(fieldId, errorMessage, 'error');
        return false;
    } else {
        showValidationMessage(fieldId, '', 'success');
        return true;
    }
}

// Validate PGP public key
function validatePGPKey(fieldId, value) {
    if (!value || value.trim() === '') {
        showValidationMessage(fieldId, 'PGP Public Key is required', 'error');
        return false;
    }
    
    // Check if it contains standard PGP public key format
    const pgpPattern = /-----BEGIN PGP PUBLIC KEY BLOCK-----[\s\S]*-----END PGP PUBLIC KEY BLOCK-----/;
    if (!pgpPattern.test(value)) {
        showValidationMessage(fieldId, 'Please enter a valid PGP public key (must include BEGIN and END markers)', 'error');
        return false;
    }
    
    // Check key length (should be at least a certain length)
    if (value.length < 200) {
        showValidationMessage(fieldId, 'PGP public key seems too short. Please paste the complete key.', 'error');
        return false;
    }
    
    showValidationMessage(fieldId, 'Valid PGP public key format', 'success');
    return true;
}

// Validate email
function validateEmail(fieldId, value) {
    if (!value || value.trim() === '') {
        return true; // Email can be empty
    }
    
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(value)) {
        showValidationMessage(fieldId, 'Please enter a valid email address', 'error');
        return false;
    } else {
        showValidationMessage(fieldId, '', 'success');
        return true;
    }
}

// Validate contact methods
function validateContactMethods(fieldName, errorMessage) {
    const checkedContacts = document.querySelectorAll(`input[name="${fieldName}"]:checked`);
    if (checkedContacts.length === 0) {
        showValidationMessage(fieldName, errorMessage, 'error');
        return false;
    } else {
        showValidationMessage(fieldName, '', 'success');
        return true;
    }
}

// Show validation message
function showValidationMessage(fieldId, message, type) {
    const validationElement = document.getElementById(`${fieldId}-validation`);
    if (validationElement) {
        validationElement.textContent = message;
        validationElement.className = type === 'error' ? 'validation-error' : 'validation-success';
        
        // Add animation effect
        if (message) {
            anime({
                targets: validationElement,
                opacity: [0, 1],
                translateY: [-10, 0],
                duration: 300,
                easing: 'easeOutQuad'
            });
        }
    }
}

// Save current step data
function saveCurrentStepData() {
    if (document.getElementById('marketExperienceEntries')) {
        updateMarketExperienceData();
    }
    
    const currentStepElement = document.getElementById(`step${currentStep}`);
    if (currentStepElement) {
        const inputs = currentStepElement.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            if (input.type === 'checkbox') {
                if (!formData[input.name]) {
                    formData[input.name] = [];
                }
                if (input.checked) {
                    formData[input.name].push(input.value);
                }
            } else {
                formData[input.name] = input.value;
            }
        });
    }
}

// Generate application summary
function generateSummary() {
    const summaryElement = document.getElementById('applicationSummary');
    if (!summaryElement) return;

    let summaryHTML = '';

    // Basic information
    if (formData.vendorName) {
        summaryHTML += `<div><strong>Vendor Name:</strong> ${formData.vendorName}</div>`;
    }
    
    if (formData.location) {
        summaryHTML += `<div><strong>Location:</strong> ${formData.location}</div>`;
    }

    const marketExperienceSummary = parseMarketExperienceData(formData.existingMarkets || document.getElementById('marketExperienceData')?.value || '[]');
    if (marketExperienceSummary.length > 0) {
        const formattedMarkets = marketExperienceSummary.map(entry => {
            const start = entry.startYear || 'N/A';
            const end = entry.endYear || 'Present';
            return `${entry.marketName || 'Unknown'} (${start} - ${end})`;
        });
        summaryHTML += `<div><strong>Market Experience:</strong> ${formattedMarkets.join(', ')}</div>`;
    }

    if (formData.experienceYears) {
        summaryHTML += `<div><strong>Total Experience:</strong> ${formData.experienceYears} years</div>`;
    }

    if (formData.productDescription) {
        summaryHTML += `<div><strong>Product Description:</strong> ${formData.productDescription}</div>`;
    }

    // Product categories
    if (formData.productCategories && formData.productCategories.length > 0) {
        summaryHTML += `<div><strong>Product Categories:</strong> ${formData.productCategories.join(', ')}</div>`;
    }

    // Contact methods
    const primaryContacts = [];
    if (formData.primaryContacts && formData.primaryContacts.length > 0) {
        formData.primaryContacts.forEach(contact => {
            const contactValue = getContactValue(contact, 'primary');
            if (contactValue) {
                primaryContacts.push(`${contact}: ${contactValue}`);
            }
        });
        if (primaryContacts.length > 0) {
            summaryHTML += `<div><strong>Primary Contact:</strong> ${primaryContacts.join(', ')}</div>`;
        }
    }

    const secondaryContacts = [];
    if (formData.secondaryContacts && formData.secondaryContacts.length > 0) {
        formData.secondaryContacts.forEach(contact => {
            const contactValue = getContactValue(contact, 'secondary');
            if (contactValue) {
                secondaryContacts.push(`${contact}: ${contactValue}`);
            }
        });
        if (secondaryContacts.length > 0) {
            summaryHTML += `<div><strong>Secondary Contact:</strong> ${secondaryContacts.join(', ')}</div>`;
        }
    }

    summaryElement.innerHTML = summaryHTML;
}

// Get contact value
function getContactValue(contactType, contactLevel) {
    const fieldMap = {
        primary: {
            telegram: 'telegramId',
            signal: 'signalId',
            jabber: 'jabberId',
            email: 'emailAddress'
        },
        secondary: {
            telegram: 'secondaryTelegramId',
            signal: 'secondarySignalId',
            jabber: 'secondaryJabberId',
            email: 'secondaryEmailAddress'
        }
    };

    const fieldId = fieldMap[contactLevel][contactType];
    return formData[fieldId] || '';
}

// Handle form submission
async function handleFormSubmit(e) {
    e.preventDefault();
    
    // Validate final step
    if (!validateCurrentStep()) {
        return;
    }

    // Save all data
    saveCurrentStepData();
    
    // Check if agree to terms
    const agreeTerms = document.getElementById('agreeTerms');
    if (!agreeTerms || !agreeTerms.checked) {
        showToast('Please agree to the Terms of Service and Privacy Policy', 'error');
        return;
    }

    // Prepare submission data
    const submissionData = prepareSubmissionData();
    
    // Show loading state
    const submitBtn = document.getElementById('submitBtn');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="animate-pulse">Submitting...</span>';
    }

    try {
        // Log submission data for debugging
        console.log('=== Submitting Application ===');
        console.log('Submission Data:', submissionData);
        
        // Call API to submit application
        const response = await API.submitVendorApplication(submissionData);
        
        // Log response for debugging
        console.log('API Response:', response);
        
        if (response.code === 200 && response.data) {
            // Success - get application ID from response
            const applicationId = response.data.applicationId || response.data.id || generateApplicationId();
            
            console.log('✅ Application submitted successfully!');
            console.log('Application ID:', applicationId);
            console.log('Database Record ID:', response.data.id);
            console.log('Full Response Data:', response.data);
            
            // Show success modal with submitted data
            showSuccessModal(applicationId, response.data);
            
            // Clear draft
            clearDraft();
            
            // Show success toast
            showToast('Application submitted successfully! Check console for details.', 'success');
        } else {
            // API returned error
            console.error('❌ API Error Response:', response);
            throw new Error(response.msg || 'Failed to submit application');
        }
    } catch (error) {
        console.error('❌ Failed to submit application:', error);
        console.error('Error details:', {
            message: error.message,
            stack: error.stack,
            submittedData: submissionData
        });
        
        showToast('Failed to submit application: ' + error.message + ' (Check console for details)', 'error');
        
        // Restore submit button
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Submit Application';
        }
    }
}

// Prepare submission data from form
function prepareSubmissionData() {
    // Ensure all form data is saved
    saveCurrentStepData();
    
    // Get all form values
    const hasMarketExperienceSelect = document.getElementById('hasMarketExperience');
    const hasMarketExperienceValue = (formData.hasMarketExperience || hasMarketExperienceSelect?.value || 'no') === 'yes';
    const marketExperienceRaw = formData.existingMarkets || document.getElementById('marketExperienceData')?.value || '[]';
    const marketExperienceList = parseMarketExperienceData(marketExperienceRaw);
    const experienceYearsInput = formData.experienceYears || document.getElementById('experienceYears')?.value;
    const computedExperienceYears = experienceYearsInput ? parseInt(experienceYearsInput, 10) : calculateExperienceYears(marketExperienceList);

    const data = {
        vendorName: formData.vendorName || document.getElementById('vendorName')?.value,
        hasMarketExperience: hasMarketExperienceValue ? 1 : 0,
        existingMarkets: marketExperienceList.length > 0 ? JSON.stringify(marketExperienceList) : null,
        experienceYears: Number.isFinite(computedExperienceYears) ? computedExperienceYears : null,
        pgpSignatureUrl: formData.pgpSignature || document.getElementById('pgpSignature')?.value,
        location: formData.location || document.getElementById('location')?.value,
        productCategories: JSON.stringify(formData.productCategories || []),
        offlineDelivery: formData.offlineDelivery === 'yes' ? 1 : 0,
        productDescription: formData.productDescription || document.getElementById('productDescription')?.value || null,
        
        // Primary contacts
        primaryTelegram: formData.telegramId || null,
        primarySignal: formData.signalId || null,
        primaryJabber: formData.jabberId || null,
        primaryEmail: formData.emailAddress || null,
        
        // Secondary contacts
        secondaryTelegram: formData.secondaryTelegramId || null,
        secondarySignal: formData.secondarySignalId || null,
        secondaryJabber: formData.secondaryJabberId || null,
        secondaryEmail: formData.secondaryEmailAddress || null,
        
        // Status
        status: 'pending',
        reviewProgress: 0
    };
    
    console.log('Prepared submission data:', data);
    console.log('Form data object:', formData);
    
    return data;
}

// Generate application ID
function generateApplicationId() {
    const timestamp = Date.now().toString();
    const random = Math.random().toString(36).substring(2, 8).toUpperCase();
    return `VENDOR-${timestamp.slice(-6)}-${random}`;
}

// Show success modal
function showSuccessModal(applicationId, submittedData) {
    console.log('showSuccessModal called with:', { applicationId, submittedData });
    
    const modal = document.getElementById('successModal');
    const idElement = document.getElementById('applicationId');
    const statusElement = document.getElementById('applicationStatus');
    
    if (!modal) {
        console.error('Success modal not found!');
        return;
    }
    
    if (!idElement) {
        console.error('Application ID element not found!');
        return;
    }
    
    // Set application ID
    idElement.textContent = applicationId || 'N/A';
    console.log('Application ID set to:', applicationId);
    
    // Set status
    if (statusElement) {
        const statusText = submittedData && submittedData.status ? submittedData.status : 'pending';
        statusElement.textContent = statusText.charAt(0).toUpperCase() + statusText.slice(1);
    }
    
    // Show modal
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    console.log('Modal displayed');
    
    // Add animation
    if (typeof anime !== 'undefined') {
        anime({
            targets: modal.querySelector('.bg-white'),
            scale: [0.8, 1],
            opacity: [0, 1],
            duration: 400,
            easing: 'easeOutBack'
        });
    }
}

// Close success modal
function closeSuccessModal() {
    const modal = document.getElementById('successModal');
    if (modal) {
        anime({
            targets: modal.querySelector('.bg-white'),
            scale: [1, 0.8],
            opacity: [1, 0],
            duration: 300,
            easing: 'easeOutQuad',
            complete: function() {
                modal.classList.add('hidden');
                modal.classList.remove('flex');
            }
        });
    }
}

// Track application status
function trackApplicationStatus() {
    const applicationId = document.getElementById('applicationId')?.textContent;
    closeSuccessModal();
    
    if (applicationId) {
        // Redirect to status page with application ID
        window.location.href = `status.html?id=${applicationId}`;
    } else {
        window.location.href = 'status.html';
    }
}

// Save draft
function saveDraft() {
    saveCurrentStepData();
    localStorage.setItem('vendorApplicationDraft', JSON.stringify({
        currentStep: currentStep,
        formData: formData
    }));
    
    // Show save success notification
    showToast('Draft saved successfully', 'success');
}

// Load draft
function loadDraft() {
    const draft = localStorage.getItem('vendorApplicationDraft');
    if (draft) {
        try {
            const draftData = JSON.parse(draft);
            formData = draftData.formData || {};
            
            // Restore form data
            Object.keys(formData).forEach(key => {
                const field = document.querySelector(`[name="${key}"]`);
                if (field) {
                    if (field.type === 'checkbox') {
                        if (Array.isArray(formData[key])) {
                            formData[key].forEach(value => {
                                const checkbox = document.querySelector(`[name="${key}"][value="${value}"]`);
                                if (checkbox) {
                                    checkbox.checked = true;
                                    if (key.includes('Contacts')) {
                                        updateContactField(checkbox, key.includes('primary') ? 'primary' : 'secondary');
                                    }
                                }
                            });
                        }
                    } else {
                        field.value = formData[key];
                    }
                }
            });

            // Restore market experience section
            if (formData.hasMarketExperience === 'yes') {
                toggleMarketExperience_internal();
            }

            // Restore step
            if (draftData.currentStep) {
                currentStep = draftData.currentStep;
                updateStepIndicators();
                updateProgressBar();
                updateNavigationButtons();
                showStep(currentStep);
            }
        } catch (e) {
            console.error('Failed to load draft:', e);
        }
    }
}

// Clear draft
function clearDraft() {
    localStorage.removeItem('vendorApplicationDraft');
}

// Show toast notification
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `fixed top-20 right-4 px-6 py-3 rounded-lg text-white z-50 ${
        type === 'success' ? 'bg-green-500' : 
        type === 'error' ? 'bg-red-500' : 'bg-blue-500'
    }`;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    anime({
        targets: toast,
        translateX: [300, 0],
        opacity: [0, 1],
        duration: 300,
        easing: 'easeOutQuad',
        complete: function() {
            setTimeout(() => {
                anime({
                    targets: toast,
                    translateX: [0, 300],
                    opacity: [1, 0],
                    duration: 300,
                    easing: 'easeOutQuad',
                    complete: function() {
                        document.body.removeChild(toast);
                    }
                });
            }, 3000);
        }
    });
}