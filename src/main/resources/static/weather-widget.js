(function() {
    const container = document.createElement('div');
    container.innerHTML = `
    <style>
    #weather-widget {
        position: fixed;
        top: 100px;
        left: 20px;
        width: 220px;
        max-width: 90%;
        background: white;
        border-radius: 10px;
        padding: 10px;
        box-shadow: 0 0 10px rgba(0,0,0,0.2);
        font-family: Arial;
        z-index: 9999;
        touch-action: none; /* Important for mobile drag */
    }
    #weather-header {
        font-weight: bold;
        cursor: grab;
        margin-bottom: 6px;
        user-select: none;
    }
    #weather-widget input {
        width: 100%;
        padding: 5px;
        margin-bottom: 5px;
        box-sizing: border-box;
    }
    #weather-widget button {
        width: 100%;
        padding: 6px;
        background: #ea580c;
        color: white;
        border: none;
        cursor: pointer;
    }
    #weather-widget button:hover {
        background: #eeb05e;
    }
    </style>
    <div id="weather-widget">
        <div id="weather-header">🌦 Weather (Drag Me)</div>
        <input id="locationInput" placeholder="ZIP or City" />
        <button id="checkWeatherBtn">Check</button>
        <div id="weatherResult"></div>
    </div>
    `;
    document.body.appendChild(container);

    const locationInput = document.getElementById("locationInput");
    const weatherResult = document.getElementById("weatherResult");
    const btn = document.getElementById("checkWeatherBtn");

    // Load last saved location
    const savedLocation = localStorage.getItem("weatherLocation");
    if (savedLocation) {
        locationInput.value = savedLocation;
        fetchWeather(savedLocation);
    }

    btn.addEventListener("click", () => {
        const loc = locationInput.value.trim();
        if (!loc) return alert("Enter ZIP or city");
        localStorage.setItem("weatherLocation", loc);
        fetchWeather(loc);
    });

    async function fetchWeather(loc) {
        try {
            const isZip = /^\d{5}$/.test(loc);
            const query = isZip ? `zip=${loc},us` : `q=${encodeURIComponent(loc)},us`;

            const response = await fetch(
                `https://api.openweathermap.org/data/2.5/weather?${query}&units=imperial&appid=70428157491abb380e0c291e48f097b3`
            );
            const data = await response.json();

            if (data.cod !== 200) {
                weatherResult.innerHTML = "Invalid location";
                return;
            }

            weatherResult.innerHTML = `
                <p><b>${data.name}</b></p>
                <p>🌡 ${data.main.temp} °F</p>
                <p>${data.weather[0].description}</p>
            `;
        } catch (error) {
            weatherResult.innerHTML = "Error loading data";
        }
    }

    // Responsive drag
    function dragElement(elmnt) {
        let posX = 0, posY = 0, startX = 0, startY = 0;

        const header = document.getElementById("weather-header");
        header.addEventListener("mousedown", dragStart, false);
        header.addEventListener("touchstart", dragStart, false);

        function dragStart(e) {
            e.preventDefault();
            if (e.type === "touchstart") {
                startX = e.touches[0].clientX;
                startY = e.touches[0].clientY;
                document.addEventListener("touchmove", dragMove, false);
                document.addEventListener("touchend", dragEnd, false);
            } else {
                startX = e.clientX;
                startY = e.clientY;
                document.addEventListener("mousemove", dragMove, false);
                document.addEventListener("mouseup", dragEnd, false);
            }
        }

        function dragMove(e) {
            let clientX = e.type.startsWith("touch") ? e.touches[0].clientX : e.clientX;
            let clientY = e.type.startsWith("touch") ? e.touches[0].clientY : e.clientY;

            posX = clientX - startX;
            posY = clientY - startY;
            startX = clientX;
            startY = clientY;

            // Update position
            let newTop = elmnt.offsetTop + posY;
            let newLeft = elmnt.offsetLeft + posX;

            // Keep inside viewport
            newTop = Math.max(0, Math.min(window.innerHeight - elmnt.offsetHeight, newTop));
            newLeft = Math.max(0, Math.min(window.innerWidth - elmnt.offsetWidth, newLeft));

            elmnt.style.top = newTop + "px";
            elmnt.style.left = newLeft + "px";
        }

        function dragEnd(e) {
            document.removeEventListener("mousemove", dragMove, false);
            document.removeEventListener("mouseup", dragEnd, false);
            document.removeEventListener("touchmove", dragMove, false);
            document.removeEventListener("touchend", dragEnd, false);
        }
    }

    dragElement(document.getElementById("weather-widget"));
})();