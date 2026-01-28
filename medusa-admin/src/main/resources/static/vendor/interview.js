// Interview Slot Booking API
// 面试预约相关 API 封装

// Extend API_ENDPOINTS
if (typeof API_ENDPOINTS !== 'undefined') {
    Object.assign(API_ENDPOINTS, {
        // Slot Booking APIs
        getAvailableSlots: '/api/mall/vendor/interview/slots/available',
        bookSlot: '/api/mall/vendor/interview/slots/',
        getMyBookings: '/api/mall/vendor/interview/my-bookings',
        getMySlots: '/api/mall/vendor/interview/my-slots'
    });
}

// Interview API Helper Functions
const InterviewAPI = {
    calendar: null,
    calendarInitialized: false,
    /**
     * Get available slots for date range
     * @param {string} startDate - Start date (yyyy-MM-dd)
     * @param {string} endDate - End date (yyyy-MM-dd)
     * @returns {Promise} Available slots list
     */
    async getAvailableSlots(startDate, endDate) {
        try {
            const endpoint = `${API_ENDPOINTS.getAvailableSlots}?startDate=${startDate}&endDate=${endDate}`;
            const response = await fetch(API.getURL(endpoint), {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'  // Use session cookie for authentication
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            return result;
        } catch (error) {
            console.error('[InterviewAPI.getAvailableSlots] Error:', error);
            throw error;
        }
    },

    /**
     * Book a slot (create interview)
     * @param {number} slotId - Slot ID
     * @param {string} applicationNumber - Application number
     * @returns {Promise} Booking result
     */
    async bookSlot(slotId, applicationNumber) {
        try {
            // 获取 CSRF token
            const getCsrfToken = () => {
                const name = 'XSRF-TOKEN';
                const cookies = document.cookie.split(';');
                for (let i = 0; i < cookies.length; i++) {
                    let cookie = cookies[i].trim();
                    if (cookie.indexOf(name + '=') === 0) {
                        return decodeURIComponent(cookie.substring(name.length + 1));
                    }
                }
                return null;
            };
            
            const csrfToken = getCsrfToken();
            const headers = {
                'Content-Type': 'application/json'
            };
            if (csrfToken) {
                headers['X-XSRF-TOKEN'] = csrfToken;
            }
            
            const endpoint = `${API_ENDPOINTS.bookSlot}${slotId}/book?applicationNumber=${encodeURIComponent(applicationNumber)}`;
            const response = await fetch(API.getURL(endpoint), {
                method: 'POST',
                headers: headers,
                credentials: 'include'  // Use session cookie for authentication
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            return result;
        } catch (error) {
            if (window.Logger) {
                window.Logger.errorSafe(error, 'InterviewAPI.bookSlot');
            }
            throw error;
        }
    },

    /**
     * Get my bookings (interviews)
     * @param {string} applicationNumber - Application number
     * @returns {Promise} Interview list
     */
    async getMyBookings(applicationNumber) {
        const token = API.getAuthToken();
        if (!token) {
            throw new Error('AUTH_REQUIRED');
        }

        try {
            const endpoint = `${API_ENDPOINTS.getMyBookings}?applicationNumber=${encodeURIComponent(applicationNumber)}`;
            const response = await fetch(API.getURL(endpoint), {
                method: 'GET',
                headers: API.buildHeaders(),
                credentials: 'include'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            return result;
        } catch (error) {
                if (window.Logger) {
                    window.Logger.errorSafe(error, 'GetMyBookings');
                }
            throw error;
        }
    },

    /**
     * Get my booked slots
     * @param {string} applicationNumber - Application number
     * @returns {Promise} Slot list
     */
    async getMySlots(applicationNumber) {
        try {
            const endpoint = `${API_ENDPOINTS.getMySlots}?applicationNumber=${encodeURIComponent(applicationNumber)}`;
            const response = await fetch(API.getURL(endpoint), {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'  // Use session cookie for authentication
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
                if (window.Logger) {
                    window.Logger.logResponse('/api/mall/vendor/interview/my-slots', result, false);
                }
            return result;
        } catch (error) {
                if (window.Logger) {
                    window.Logger.errorSafe(error, 'InterviewAPI.getMySlots');
                }
            throw error;
        }
    },

    /**
     * Format date for display
     * @param {string|Date} date - Date to format
     * @returns {string} Formatted date string
     */
    formatDate(date) {
        if (!date) return '';
        const d = new Date(date);
        return d.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    /**
     * Format time slot for display
     * @param {string|Date} startTime - Slot start time
     * @param {string|Date} endTime - Slot end time
     * @returns {string} Formatted time string
     */
    formatTimeSlot(startTime, endTime) {
        if (!startTime || !endTime) return '';
        const start = new Date(startTime);
        const end = new Date(endTime);
        const startStr = start.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        const endStr = end.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        return `${startStr} - ${endStr}`;
    },

    /**
     * Check if slot is in the past
     * @param {string|Date} slotStart - Slot start time
     * @returns {boolean} True if slot is in the past
     */
    isSlotPast(slotStart) {
        if (!slotStart) return false;
        return new Date(slotStart) < new Date();
    },

    /**
     * Initialize slot calendar inside booking modal
     * @param {HTMLElement} calendarEl - Calendar container element
     * @param {HTMLElement} loadingEl - Loading indicator element
     * @param {HTMLElement} emptyEl - Empty state container
     */
    initCalendar(calendarEl, loadingEl, emptyEl) {
        if (!calendarEl || typeof FullCalendar === 'undefined') {
            if (window.Logger) {
                window.Logger.warn('FullCalendar not loaded or element missing');
            }
            return;
        }

        if (InterviewAPI.calendar) {
            InterviewAPI.calendar.destroy();
            InterviewAPI.calendar = null;
        }

        InterviewAPI.calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'timeGridWeek',
            height: 'auto',
            aspectRatio: 1.5,
            headerToolbar: {
                left: 'title',
                center: '',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
            },
            firstDay: 1,
            slotMinTime: '07:00:00',
            slotMaxTime: '23:00:00',
            allDaySlot: false,
            displayEventEnd: true,
            eventClick: info => InterviewAPI.handleCalendarEventClick(info.event),
            events: (fetchInfo, success, failure) => InterviewAPI.loadCalendarEvents(fetchInfo, success, failure, loadingEl, emptyEl)
        });

        InterviewAPI.calendar.render();
        InterviewAPI.calendarInitialized = true;
    },

    /**
     * Load events for calendar
     */
    async loadCalendarEvents(fetchInfo, successCallback, failureCallback, loadingEl, emptyEl) {
        try {
            if (loadingEl) loadingEl.classList.remove('hidden');
            if (emptyEl) emptyEl.classList.add('hidden');

            const start = fetchInfo.startStr.slice(0, 10);
            const end = fetchInfo.endStr.slice(0, 10);
            const result = await InterviewAPI.getAvailableSlots(start, end);

            const events = (result.data || []).map(slot => ({
                id: slot.id,
                title: InterviewAPI.formatTimeSlot(slot.slotStart, slot.slotEnd),
                start: slot.slotStart.replace(' ', 'T'),
                end: slot.slotEnd.replace(' ', 'T'),
                backgroundColor: '#34D399',
                borderColor: '#059669',
                textColor: '#fff',
                extendedProps: { slot }
            }));

            if (events.length === 0 && emptyEl) {
                emptyEl.classList.remove('hidden');
            }

            successCallback(events);
        } catch (error) {
            if (window.Logger) {
                window.Logger.errorSafe(error, 'LoadCalendarEvents');
            }
            failureCallback(error);
            alert('Failed to load available interview slots, please try again later.');
        } finally {
            if (loadingEl) loadingEl.classList.add('hidden');
        }
    },

    /**
     * Handle calendar event click (book slot)
     */
    async handleCalendarEventClick(event) {
        const slot = event.extendedProps.slot;
        if (!slot || !window.currentApplicationNumber) {
            alert('Application number not found, please search your application first.');
            return;
        }

        const dateStr = InterviewAPI.formatDate(slot.slotStart);
        const timeStr = InterviewAPI.formatTimeSlot(slot.slotStart, slot.slotEnd);

        if (!confirm(`Confirm to book the interview slot on ${dateStr} (${timeStr})?`)) {
            return;
        }

        try {
            const result = await InterviewAPI.bookSlot(slot.id, window.currentApplicationNumber);
                if (window.Logger) {
                    window.Logger.log('Book slot result received');
                }
            
            if (result.code === 200) {
                alert('Slot booked successfully! Our CA will confirm with you soon.');
                
                // Close modal first
                if (typeof closeBookingModal === 'function') {
                    closeBookingModal();
                } else if (window.closeBookingModal) {
                    window.closeBookingModal();
                }
                
                // Refresh calendar
                if (InterviewAPI.calendar) {
                    InterviewAPI.calendar.refetchEvents();
                }

                // Wait a bit for backend to process, then reload
                if (window.Logger) {
                    window.Logger.log('Waiting before reloading application details...');
                }
                setTimeout(async () => {
                    if (window.Logger) {
                        window.Logger.log('Reloading application detail');
                    }
                    if (typeof loadApplicationDetail === 'function') {
                        await loadApplicationDetail(window.currentApplicationNumber);
                    } else if (window.loadApplicationDetail) {
                        await window.loadApplicationDetail(window.currentApplicationNumber);
                    } else {
                        if (window.Logger) {
                            window.Logger.error('loadApplicationDetail function not found');
                        }
                    }
                }, 2000);
            } else {
                alert(`Failed to book slot: ${result.msg || 'Unknown error'}`);
            }
        } catch (error) {
            if (window.Logger) {
                window.Logger.errorSafe(error, 'BookSlot');
            }
            if (error && error.message === 'AUTH_REQUIRED') {
                alert('Please login via the OS2 portal before booking an interview.');
            } else {
                alert(`Failed to book slot: ${error.message}`);
            }
        }
    }
};

// Export to global scope
window.InterviewAPI = InterviewAPI;

if (window.Logger) {
    window.Logger.log('Interview API loaded');
}

