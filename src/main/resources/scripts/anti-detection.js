(function () {
  // Only apply anti-detection to sites that specifically need it
  // Note: %SITES_PLACEHOLDER% will be replaced at runtime with the actual site list 
  // @formatter:off
  // noinspection JSMismatchedCollectionQueryUpdate
  const needsAntiDetection = /* %SITES_PLACEHOLDER% */ [];
  // @formatter:on

  const currentHost = location.hostname.toLowerCase();
  const needsProtection = needsAntiDetection.some(domain =>
    currentHost.includes(domain) || currentHost.endsWith(domain)
  );

  if (!needsProtection) {
    return; // Skip anti-detection for most sites
  }

  // Remove automation indicators
  if (typeof window.webdriver !== 'undefined') {
    delete window.webdriver;
  }

  // Hide automation properties
  Object.defineProperty(navigator, 'webdriver', {
    get: () => undefined,
    configurable: true
  });

  // Hide automation flags
  ['__webdriver_evaluate', '__selenium_evaluate', '__webdriver_script_function',
    '__webdriver_script_func', '__webdriver_script_fn', '__fxdriver_evaluate',
    '__driver_unwrapped', '__webdriver_unwrapped', '__driver_evaluate',
    '__selenium_unwrapped', '__fxdriver_unwrapped'].forEach(prop => {
    if (window[prop]) {
      delete window[prop];
    }
  });

  // Spoof Chrome runtime
  if (!window.chrome) {
    window.chrome = {};
  }

  if (!window.chrome.runtime) {
    // noinspection JSUnusedGlobalSymbols
    Object.defineProperty(window.chrome, 'runtime', {
      get: () => ({
        onConnect: undefined,
        onMessage: undefined,
        sendMessage: () => {
        },
        connect: () => ({
          onMessage: {
            addListener: () => {
            }, removeListener: () => {
            }
          },
          onDisconnect: {
            addListener: () => {
            }, removeListener: () => {
            }
          },
          postMessage: () => {
          }
        })
      }),
      configurable: true
    });
  }

  // Enhanced plugins spoofing
  // Note: PluginArray is deprecated, but we need to mimic it for anti-detection
  Object.defineProperty(navigator, 'plugins', {
    get: () => {
      const plugins = [
        {name: 'Chrome PDF Plugin', description: 'Portable Document Format', filename: 'internal-pdf-viewer', length: 1},
        {name: 'Chrome PDF Viewer', description: 'Portable Document Format', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', length: 1},
        {name: 'Native Client', description: 'Native Client Executable', filename: 'internal-nacl-plugin', length: 2}
      ];

      // Use the existing plugins prototype if available, otherwise create a mock
      // noinspection JSDeprecatedSymbols
      const proto = navigator.plugins && navigator.plugins.constructor ?
        Object.getPrototypeOf(navigator.plugins) :
        Array.prototype;

      return Object.setPrototypeOf(plugins, proto);
    },
    configurable: true
  });

  // Mock WebGL to avoid fingerprinting
  const getParameter = WebGLRenderingContext.prototype.getParameter;
  WebGLRenderingContext.prototype.getParameter = function (parameter) {
    if (parameter === 37445) {
      return 'Intel Inc.';
    }
    if (parameter === 37446) {
      return 'Intel(R) Iris(TM) Graphics 6100';
    }
    return getParameter.call(this, parameter);
  };

  // Override permissions API for better compatibility
  if (navigator.permissions && navigator.permissions.query) {
    const originalQuery = navigator.permissions.query;
    navigator.permissions.query = (parameters) => {
      return parameters.name === 'notifications' ?
        Promise.resolve({state: 'default'}) :
        originalQuery.call(navigator.permissions, parameters);
    };
  }

  // Mock realistic connection
  Object.defineProperty(navigator, 'connection', {
    get: () => ({
      effectiveType: '4g',
      rtt: 50 + Math.random() * 100,
      downlink: 5 + Math.random() * 20,
      saveData: false
    }),
    configurable: true
  });

  // Mock battery with realistic values
  if (!navigator.getBattery) {
    navigator.getBattery = () => Promise.resolve({
      charging: Math.random() > 0.5,
      chargingTime: Math.random() * 3600,
      dischargingTime: 14400 + Math.random() * 7200,
      level: 0.5 + Math.random() * 0.5
    });
  }

  // Consistent language settings
  Object.defineProperty(navigator, 'language', {
    get: () => 'en-US',
    configurable: true
  });

  Object.defineProperty(navigator, 'languages', {
    get: () => ['en-US', 'en'],
    configurable: true
  });

  // Realistic hardware specs
  Object.defineProperty(navigator, 'hardwareConcurrency', {
    get: () => Math.max(2, Math.min(16, navigator.hardwareConcurrency || 4)),
    configurable: true
  });

  // Memory info spoofing
  Object.defineProperty(navigator, 'deviceMemory', {
    get: () => 8,
    configurable: true
  });

  // Hide automation in the console
  const originalConsoleDebug = console.debug;
  console.debug = function (...args) {
    if (args.length > 0 && typeof args[0] === 'string' &&
      (args[0].includes('DevTools') || args[0].includes('automation'))) {
      return;
    }
    return originalConsoleDebug.apply(console, args);
  };

  // Mouse and keyboard event simulation
  ['mouse', 'keyboard'].forEach(eventType => {
    const events = eventType === 'mouse' ?
      ['click', 'mousedown', 'mouseup', 'mousemove'] :
      ['keydown', 'keyup', 'keypress'];

    events.forEach(event => {
      const originalAddEventListener = EventTarget.prototype.addEventListener;
      EventTarget.prototype.addEventListener = function (type, listener, options) {
        if (type === event && typeof listener === 'function') {
          const wrappedListener = function (e) {
            // Add realistic timing
            if (!e.isTrusted) {
              Object.defineProperty(e, 'isTrusted', {value: true, configurable: false});
            }
            return listener.call(this, e);
          };
          return originalAddEventListener.call(this, type, wrappedListener, options);
        }
        return originalAddEventListener.call(this, type, listener, options);
      };
    });
  });
})();