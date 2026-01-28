// Global variables
let particleApp;
let particles = [];

// Initialize after page load
document.addEventListener('DOMContentLoaded', function() {
    initParticleBackground();
    initAnimations();
    initChart();
    initCounters();
    initScrollEffects();
});

// Initialize particle background
function initParticleBackground() {
    const canvas = document.getElementById('particles-canvas');
    if (!canvas) return;

    // Create PIXI application
    particleApp = new PIXI.Application({
        view: canvas,
        width: window.innerWidth,
        height: window.innerHeight,
        transparent: true,
        antialias: true
    });

    // Create particle container
    const particleContainer = new PIXI.Container();
    particleApp.stage.addChild(particleContainer);

    // Create particles
    for (let i = 0; i < 50; i++) {
        const particle = createParticle();
        particleContainer.addChild(particle);
        particles.push(particle);
    }

    // Animation loop
    particleApp.ticker.add(updateParticles);
}

// Create single particle
function createParticle() {
    const graphics = new PIXI.Graphics();
    const size = Math.random() * 3 + 1;
    const alpha = Math.random() * 0.5 + 0.2;
    
    graphics.beginFill(0x3b82f6, alpha);
    graphics.drawCircle(0, 0, size);
    graphics.endFill();
    
    graphics.x = Math.random() * window.innerWidth;
    graphics.y = Math.random() * window.innerHeight;
    graphics.vx = (Math.random() - 0.5) * 0.5;
    graphics.vy = (Math.random() - 0.5) * 0.5;
    
    return graphics;
}

// Update particle positions
function updateParticles() {
    particles.forEach(particle => {
        particle.x += particle.vx;
        particle.y += particle.vy;
        
        // Boundary detection
        if (particle.x < 0 || particle.x > window.innerWidth) {
            particle.vx *= -1;
        }
        if (particle.y < 0 || particle.y > window.innerHeight) {
            particle.vy *= -1;
        }
        
        // Keep within bounds
        particle.x = Math.max(0, Math.min(window.innerWidth, particle.x));
        particle.y = Math.max(0, Math.min(window.innerHeight, particle.y));
    });
}

// Initialize animations
function initAnimations() {
    // Hero section animation
    anime({
        targets: '.floating-element',
        translateY: [-20, 0],
        opacity: [0, 1],
        duration: 1000,
        easing: 'easeOutQuad',
        delay: 500
    });

    // Feature cards animation
    anime({
        targets: '.card-hover',
        translateY: [50, 0],
        opacity: [0, 1],
        duration: 800,
        easing: 'easeOutQuad',
        delay: anime.stagger(100, {start: 1000})
    });

    // Process steps animation
    anime({
        targets: '#process .text-center',
        scale: [0.8, 1],
        opacity: [0, 1],
        duration: 600,
        easing: 'easeOutBack',
        delay: anime.stagger(200, {start: 1500})
    });
}

// Initialize chart
function initChart() {
    const chartDom = document.getElementById('chart-container');
    if (!chartDom) return;

    const myChart = echarts.init(chartDom);
    
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        legend: {
            data: ['Applications', 'Approvals']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: [
            {
                type: 'category',
                boundaryGap: false,
                data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
            }
        ],
        yAxis: [
            {
                type: 'value'
            }
        ],
        series: [
            {
                name: 'Applications',
                type: 'line',
                stack: 'Total',
                smooth: true,
                lineStyle: {
                    color: '#1e3a8a'
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: 'rgba(30, 58, 138, 0.3)'
                        }, {
                            offset: 1, color: 'rgba(30, 58, 138, 0.1)'
                        }]
                    }
                },
                emphasis: {
                    focus: 'series'
                },
                data: [120, 132, 101, 134, 90, 230, 210, 182, 191, 234, 290, 330]
            },
            {
                name: 'Approvals',
                type: 'line',
                stack: 'Total',
                smooth: true,
                lineStyle: {
                    color: '#f59e0b'
                },
                areaStyle: {
                    color: {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: 'rgba(245, 158, 11, 0.3)'
                        }, {
                            offset: 1, color: 'rgba(245, 158, 11, 0.1)'
                        }]
                    }
                },
                emphasis: {
                    focus: 'series'
                },
                data: [80, 95, 75, 98, 65, 160, 150, 125, 135, 168, 210, 245]
            }
        ]
    };

    myChart.setOption(option);

    // Responsive adjustment
    window.addEventListener('resize', function() {
        myChart.resize();
    });
}

// Initialize counter animations
function initCounters() {
    const counters = [
        { id: 'total-applications', target: 2847, suffix: '' },
        { id: 'success-rate', target: 87, suffix: '%' },
        { id: 'avg-process-time', target: 3, suffix: '' },
        { id: 'active-vendors', target: 1256, suffix: '' }
    ];

    counters.forEach(counter => {
        const element = document.getElementById(counter.id);
        if (!element) return;

        anime({
            targets: { count: 0 },
            count: counter.target,
            duration: 2000,
            easing: 'easeOutQuad',
            delay: 2000,
            update: function(anim) {
                element.textContent = Math.floor(anim.animatables[0].target.count) + counter.suffix;
            }
        });
    });
}

// Initialize scroll effects
function initScrollEffects() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe elements that need animation
    document.querySelectorAll('.card-hover, #process .text-center').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
}

// Resize particle system on window resize
window.addEventListener('resize', function() {
    if (particleApp) {
        particleApp.renderer.resize(window.innerWidth, window.innerHeight);
    }
});

// Navigation functions
function startApplication() {
    // Add click animation
    anime({
        targets: event.target,
        scale: [1, 0.95, 1],
        duration: 200,
        easing: 'easeInOutQuad',
        complete: function() {
            window.location.href = 'application.html';
        }
    });
}

function trackStatus() {
    // Add click animation
    anime({
        targets: event.target,
        scale: [1, 0.95, 1],
        duration: 200,
        easing: 'easeInOutQuad',
        complete: function() {
            window.location.href = 'status.html';
        }
    });
}

function openAdminPanel() {
    // Add click animation
    anime({
        targets: event.target,
        scale: [1, 0.95, 1],
        duration: 200,
        easing: 'easeInOutQuad',
        complete: function() {
            window.location.href = 'admin.html';
        }
    });
}

// Smooth scroll to section
function scrollToSection(sectionId) {
    const element = document.getElementById(sectionId);
    if (element) {
        element.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

// Add navigation link click events
document.addEventListener('DOMContentLoaded', function() {
    const navLinks = document.querySelectorAll('nav a[href^="#"]');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            scrollToSection(targetId);
        });
    });
});

// Welcome animation after page load
window.addEventListener('load', function() {
    anime({
        targets: 'body',
        opacity: [0, 1],
        duration: 500,
        easing: 'easeOutQuad'
    });
});