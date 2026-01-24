class FSRWebSocket {
    constructor() {
        this.ws = null;
        this.isReady = false;
        this.callbacks = {};
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws`;

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('WebSocket connected');
            this.isReady = true;
            this.reconnectAttempts = 0;
        };

        this.ws.onmessage = (event) => {
            const data = JSON.parse(event.data);
            const action = data[0];
            const message = data[1];

            if (this.callbacks[action]) {
                this.callbacks[action](message);
            }
        };

        this.ws.onclose = () => {
            console.log('WebSocket disconnected');
            this.isReady = false;
            this.handleReconnect();
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };
    }

    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            setTimeout(() => {
                this.reconnectAttempts++;
                this.connect();
            }, 1000 * this.reconnectAttempts);
        }
    }

    emit(message) {
        if (this.isReady && this.ws) {
            this.ws.send(JSON.stringify(message));
        }
    }

    on(action, callback) {
        this.callbacks[action] = callback;
    }
}

// Instancia global
const fsrWebSocket = new FSRWebSocket();