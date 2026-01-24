class ProfileControls {
    constructor(websocket) {
        this.websocket = websocket;
        this.profiles = [];
        this.currentProfile = '';
        this.buttonNames = ['Left', 'Down', 'Up', 'Right'];
    }

    initialize() {
        this.setupEventListeners();
        this.setupWebSocketListeners();
    }

    setupEventListeners() {
        // Formulario para añadir perfil
        const addProfileForm = document.getElementById('add-profile-form');
        if (addProfileForm) {
            addProfileForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.addProfile();
            });
        }

        // Botón de guardar umbrales
        const saveButton = document.getElementById('save-thresholds');
        if (saveButton) {
            saveButton.addEventListener('click', () => {
                this.saveThresholds();
            });
        }
    }

    setupWebSocketListeners() {
        this.websocket.on('get_profiles', (msg) => {
            this.updateProfileList(msg);
        });

        this.websocket.on('get_cur_profile', (msg) => {
            this.updateCurrentProfile(msg);
        });

        this.websocket.on('thresholds_persisted', () => {
            this.showSuccessAlert('Umbrales guardados exitosamente');
        });
    }

    updateProfileList(data) {
        this.profiles = data.profiles || [];
        this.currentProfile = data.cur_profile || '';

        const dropdown = document.getElementById('profile-dropdown');
        if (!dropdown) return;

        dropdown.innerHTML = '';

        this.profiles.forEach(profile => {
            const isActive = profile === this.currentProfile;
            const item = document.createElement('a');
            item.className = 'dropdown-item' + (isActive ? ' active' : '');
            item.href = '#';
            item.innerHTML = `
                <button class="btn btn-sm btn-light mr-2" onclick="profileControls.removeProfile('${profile}')">X</button>
                ${profile}
            `;
            item.onclick = (e) => {
                e.preventDefault();
                if (!e.target.closest('button')) {
                    this.changeProfile(profile);
                }
            };
            dropdown.appendChild(item);
        });
    }

    updateCurrentProfile(data) {
        this.currentProfile = data.cur_profile || '';

        // Actualizar estado activo en el dropdown
        document.querySelectorAll('#profile-dropdown .dropdown-item').forEach(item => {
            item.classList.remove('active');
        });
    }

    addProfile() {
        const input = document.getElementById('new-profile-name');
        const profileName = input.value.trim();

        if (profileName) {
            const thresholds = this.getCurrentThresholds();
            this.websocket.emit(['add_profile', profileName, thresholds]);
            input.value = '';
        }
    }

    removeProfile(profileName) {
        if (confirm(`¿Eliminar perfil "${profileName}"?`)) {
            this.websocket.emit(['remove_profile', profileName]);
        }
    }

    changeProfile(profileName) {
        this.websocket.emit(['change_profile', profileName]);
    }

    saveThresholds() {
        this.websocket.emit(['save_thresholds']);
    }

    getCurrentThresholds() {
        const thresholds = [];
        document.querySelectorAll('.threshold-label').forEach(label => {
            thresholds.push(parseInt(label.textContent));
        });
        return thresholds;
    }

    showSuccessAlert(message) {
        // Crear o mostrar alerta de éxito
        let alert = document.getElementById('profile-success-alert');
        if (!alert) {
            alert = document.createElement('div');
            alert.id = 'profile-success-alert';
            alert.className = 'alert alert-success alert-dismissible fade show';
            alert.style.position = 'fixed';
            alert.style.top = '20px';
            alert.style.right = '20px';
            alert.style.zIndex = '9999';
            alert.innerHTML = `
                <strong>¡Éxito!</strong> <span id="profile-success-message">${message}</span>
                <button type="button" class="close" data-dismiss="alert">
                    <span>&times;</span>
                </button>
            `;
            document.body.appendChild(alert);
        } else {
            document.getElementById('profile-success-message').textContent = message;
            alert.style.display = 'block';
        }

        // Auto-ocultar después de 3 segundos
        setTimeout(() => {
            alert.style.display = 'none';
        }, 3000);
    }
}

// Instancia global
let profileControls;

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    profileControls = new ProfileControls(window.fsrWebSocket);
    profileControls.initialize();
});