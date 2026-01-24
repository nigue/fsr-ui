class PlotCanvas {
    constructor(websocket) {
        this.websocket = websocket;
        this.canvas = null;
        this.ctx = null;
        this.numSensors = 4;
        this.display = new Array(this.numSensors).fill(true);
        this.maxSize = 1000;
        this.curValues = [];
        this.curThresholds = [];
        this.oldest = 0;

        // Colores para sensores
        this.degreesPerSensor = 360 / this.numSensors;
        this.colors = [...Array(this.numSensors)].map((_, i) =>
            `hsl(${this.degreesPerSensor * i}, 100%, 40%)`);
        this.darkColors = [...Array(this.numSensors)].map((_, i) =>
            `hsl(${this.degreesPerSensor * i}, 100%, 35%)`);
    }

    initialize() {
        this.canvas = document.getElementById('plot-canvas');
        if (!this.canvas) return;

        this.ctx = this.canvas.getContext('2d');
        this.setupCanvas();
        this.setupEventListeners();
        this.startAnimation();
    }

    setupCanvas() {
        const rect = this.canvas.getBoundingClientRect();
        const dpi = window.devicePixelRatio || 1;

        this.canvas.width = rect.width * dpi;
        this.canvas.height = rect.height * dpi;
        this.ctx.scale(dpi, dpi);
    }

    setupEventListeners() {
        window.addEventListener('resize', () => this.setupCanvas());

        // Botones de toggle para mostrar/ocultar sensores
        document.querySelectorAll('.toggle-sensor').forEach((button, index) => {
            button.addEventListener('click', () => {
                this.toggleSensor(index);
            });
        });
    }

    toggleSensor(index) {
        this.display[index] = !this.display[index];
        const button = document.querySelectorAll('.toggle-sensor')[index];
        button.classList.toggle('active', this.display[index]);
        button.style.color = this.display[index] ? this.darkColors[index] : '#f8f9fa';
    }

    updateValues(values) {
        if (this.curValues.length < this.maxSize) {
            this.curValues.push(values);
        } else {
            this.curValues[this.oldest] = values;
            this.oldest = (this.oldest + 1) % this.maxSize;
        }
    }

    updateThresholds(thresholds) {
        this.curThresholds = thresholds;
    }

    drawDashedLine(pattern, spacing, y, width) {
        this.ctx.beginPath();
        this.ctx.setLineDash(pattern);
        this.ctx.moveTo(spacing, y);
        this.ctx.lineTo(width, y);
        this.ctx.stroke();
    }

    render() {
        if (!this.ctx) return;

        const rect = this.canvas.getBoundingClientRect();
        const width = rect.width;
        const height = rect.height;

        // Fondo
        this.ctx.fillStyle = '#f8f9fa';
        this.ctx.fillRect(0, 0, width, height);

        // Borde
        const spacing = 10;
        const boxWidth = width - spacing * 2;
        const boxHeight = height - spacing * 2;

        this.ctx.strokeStyle = 'darkgray';
        this.ctx.beginPath();
        this.ctx.rect(spacing, spacing, boxWidth, boxHeight);
        this.ctx.stroke();

        // Divisiones del gráfico
        const minorDivision = 100;
        for (let i = 1; i * minorDivision < 1023; ++i) {
            const pattern = i % 2 === 0 ? [20, 5] : [5, 10];
            this.drawDashedLine(pattern, spacing,
                boxHeight - (boxHeight * (i * minorDivision) / 1023) + spacing,
                boxWidth + spacing);
        }

        // Graficar líneas para cada sensor
        const pxPerDiv = boxWidth / this.maxSize;
        let plotNums = 0;
        for (let i = 0; i < this.numSensors; ++i) {
            if (this.display[i]) {
                ++plotNums;
            }
        }

        let k = -1;
        for (let i = 0; i < this.numSensors; ++i) {
            if (this.display[i]) {
                ++k;
                this.ctx.beginPath();
                this.ctx.setLineDash([]);
                this.ctx.strokeStyle = this.colors[i];
                this.ctx.lineWidth = 2;

                for (let j = 0; j < this.maxSize; ++j) {
                    if (j === this.curValues.length) break;

                    let yValue = (
                        boxHeight
                        - boxHeight * this.curValues[(j + this.oldest) % this.maxSize][i] / 1023 / plotNums
                        - k / plotNums * boxHeight
                        + spacing
                    );

                    if (j === 0) {
                        this.ctx.moveTo(spacing, yValue);
                    } else {
                        this.ctx.lineTo(pxPerDiv * j + spacing, yValue);
                    }
                }
                this.ctx.stroke();
            }
        }

        // Mostrar umbrales actuales
        k = -1;
        for (let i = 0; i < this.numSensors; ++i) {
            if (this.display[i]) {
                ++k;
                this.ctx.beginPath();
                this.ctx.setLineDash([]);
                this.ctx.strokeStyle = this.darkColors[i];
                this.ctx.lineWidth = 2;

                let yValue = (
                    boxHeight
                    - boxHeight * this.curThresholds[i] / 1023 / plotNums
                    - k / plotNums * boxHeight
                    + spacing
                );

                this.ctx.moveTo(spacing, yValue);
                this.ctx.lineTo(boxWidth + spacing, yValue);
                this.ctx.stroke();
            }
        }

        // Mostrar valores actuales
        this.ctx.font = '30px Arial';
        for (let i = 0; i < this.numSensors; ++i) {
            if (this.display[i]) {
                this.ctx.fillStyle = this.colors[i];
                let currentValue;
                if (this.curValues.length < this.maxSize) {
                    currentValue = this.curValues[this.curValues.length - 1][i];
                } else {
                    currentValue = this.curValues[((this.oldest - 1) % this.maxSize + this.maxSize) % this.maxSize][i];
                }
                this.ctx.fillText(currentValue, 100 + i * 100, 100);
            }
        }
    }

    startAnimation() {
        const animate = () => {
            this.render();
            requestAnimationFrame(animate);
        };
        animate();
    }
}

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    const plotCanvas = new PlotCanvas(window.fsrWebSocket);

    // Escuchar eventos WebSocket
    window.fsrWebSocket.on('values', function(msg) {
        plotCanvas.updateValues(msg);
    });

    window.fsrWebSocket.on('thresholds', function(msg) {
        plotCanvas.updateThresholds(msg);
    });

    // Inicializar cuando esté listo
    setTimeout(() => plotCanvas.initialize(), 100);
});