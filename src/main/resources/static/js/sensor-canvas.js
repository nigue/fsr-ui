class SensorMonitor {
    constructor(index, websocket) {
        this.index = index;
        this.websocket = websocket;
        this.canvas = null;
        this.ctx = null;
        this.thresholdLabel = null;
        this.valueLabel = null;
        this.isDragging = false;
        this.currentThreshold = 0;
        this.currentValue = 0;
    }

    initialize(container) {
        // Clonar el fragmento del sensor
        const fragment = document.createElement('div');
        fragment.innerHTML = `
            <div class="sensor-monitor-col" data-index="${this.index}">
                <div class="sensor-controls">
                    <button class="btn btn-sm btn-light decrement-btn">-</button>
                    <span> </span>
                    <button class="btn btn-sm btn-light increment-btn">+</button>
                </div>
                <div class="sensor-label threshold-label">0</div>
                <div class="sensor-label value-label">0</div>
                <canvas class="sensor-canvas"></canvas>
            </div>
        `;

        container.appendChild(fragment.firstElementChild);

        this.setupElements();
        this.setupEventListeners();
        this.startAnimation();
    }

    setupElements() {
        const element = document.querySelector(`[data-index="${this.index}"]`);
        this.canvas = element.querySelector('.sensor-canvas');
        this.ctx = this.canvas.getContext('2d');
        this.thresholdLabel = element.querySelector('.threshold-label');
        this.valueLabel = element.querySelector('.value-label');

        this.setupCanvas();
    }

    setupCanvas() {
        const rect = this.canvas.getBoundingClientRect();
        const dpr = window.devicePixelRatio || 1;

        this.canvas.width = rect.width * dpr;
        this.canvas.height = rect.height * dpr;

        this.ctx.scale(dpr, dpr);
    }

    setupEventListeners() {
        const element = document.querySelector(`[data-index="${this.index}"]`);

        // Botones de incremento/decremento
        element.querySelector('.decrement-btn').addEventListener('click', () => {
            this.updateThreshold(this.currentThreshold - 1);
        });

        element.querySelector('.increment-btn').addEventListener('click', () => {
            this.updateThreshold(this.currentThreshold + 1);
        });

        // Eventos del canvas
        this.canvas.addEventListener('mousedown', (e) => this.handleMouseDown(e));
        this.canvas.addEventListener('mousemove', (e) => this.handleMouseMove(e));
        this.canvas.addEventListener('mouseup', () => this.handleMouseUp());
        this.canvas.addEventListener('touchstart', (e) => this.handleTouchStart(e));
        this.canvas.addEventListener('touchmove', (e) => this.handleTouchMove(e));
        this.canvas.addEventListener('touchend', () => this.handleTouchEnd());
    }

    handleMouseDown(e) {
        const rect = this.canvas.getBoundingClientRect();
        const y = e.clientY - rect.top;
        this.currentThreshold = Math.floor(1023 - (y / rect.height * 1023));
        this.isDragging = true;
    }

    handleMouseMove(e) {
        if (!this.isDragging) return;

        const rect = this.canvas.getBoundingClientRect();
        const y = e.clientY - rect.top;
        this.currentThreshold = Math.floor(1023 - (y / rect.height * 1023));
    }

    handleMouseUp() {
        if (this.isDragging) {
            this.emitThreshold();
            this.isDragging = false;
        }
    }

    handleTouchStart(e) {
        e.preventDefault();
        const rect = this.canvas.getBoundingClientRect();
        const touch = e.touches[0];
        const y = touch.clientY - rect.top;
        this.currentThreshold = Math.floor(1023 - (y / rect.height * 1023));
        this.isDragging = true;
    }

    handleTouchMove(e) {
        e.preventDefault();
        if (!this.isDragging) return;

        const rect = this.canvas.getBoundingClientRect();
        const touch = e.touches[0];
        const y = touch.clientY - rect.top;
        this.currentThreshold = Math.floor(1023 - (y / rect.height * 1023));
    }

    handleTouchEnd() {
        if (this.isDragging) {
            this.emitThreshold();
            this.isDragging = false;
        }
    }

    updateThreshold(value) {
        if (value >= 0 && value <= 1023) {
            this.currentThreshold = value;
            this.emitThreshold();
        }
    }

    emitThreshold() {
        // Obtener todos los umbrales actuales
        const thresholds = Array.from(document.querySelectorAll('.threshold-label'))
            .map(label => parseInt(label.textContent));

        this.websocket.emit(['update_threshold', thresholds, this.index]);
    }

    updateValue(value) {
        this.currentValue = value;
        this.valueLabel.textContent = value;
    }

    updateThresholdDisplay(threshold) {
        this.currentThreshold = threshold;
        this.thresholdLabel.textContent = threshold;
    }

    startAnimation() {
        const render = () => {
            this.draw();
            requestAnimationFrame(render);
        };
        render();
    }

    draw() {
        const rect = this.canvas.getBoundingClientRect();
        const width = rect.width;
        const height = rect.height;

        // Limpiar canvas
        this.ctx.clearRect(0, 0, width, height);

        // Fondo gradiente
        const gradient = this.ctx.createLinearGradient(0, 0, 0, height);
        if (this.currentValue >= this.currentThreshold) {
            gradient.addColorStop(0, '#87CEEB');
            gradient.addColorStop(1, '#0066CC');
        } else {
            gradient.addColorStop(0, '#87CEEB');
            gradient.addColorStop(1, '#808080');
        }

        this.ctx.fillStyle = gradient;
        this.ctx.fillRect(0, 0, width, height);

        // Barra de valor
        const barHeight = height - (this.currentValue / 1023 * height);
        const barGradient = this.ctx.createLinearGradient(0, height, 0, barHeight);
        barGradient.addColorStop(0, '#FFA500');
        barGradient.addColorStop(1, '#FF0000');

        this.ctx.fillStyle = barGradient;
        this.ctx.fillRect(width / 4, barHeight, width / 2, height - barHeight);

        // Línea de umbral
        const thresholdY = height - (this.currentThreshold / 1023 * height);
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(0, thresholdY - 1, width, 3);

        // Etiqueta de umbral
        this.ctx.fillStyle = '#000000';
        this.ctx.font = '30px Arial';
        if (this.currentThreshold > 990) {
            this.ctx.textBaseline = 'top';
        } else {
            this.ctx.textBaseline = 'bottom';
        }
        this.ctx.fillText(this.currentThreshold.toString(), 0, thresholdY + 4);
    }
}