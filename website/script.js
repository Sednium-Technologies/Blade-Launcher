document.documentElement.classList.add('js');

document.addEventListener('DOMContentLoaded', function () {

  /* ---------- Theme Toggle (Dark / Light) ---------- */
  var themeToggleBtn = document.getElementById('themeToggle');
  var currentTheme = localStorage.getItem('blade_theme') || 
    (window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark');

  function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('blade_theme', theme);
  }

  applyTheme(currentTheme);

  if (themeToggleBtn) {
    themeToggleBtn.addEventListener('click', function () {
      var newTheme = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
      applyTheme(newTheme);
    });
  }

  /* ---------- Mobile Nav Toggle ---------- */
  var toggle = document.getElementById('navToggle');
  var links = document.getElementById('navLinks');
  if (toggle && links) {
    toggle.addEventListener('click', function () {
      var open = links.classList.toggle('open');
      toggle.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
    links.querySelectorAll('a').forEach(function (a) {
      a.addEventListener('click', function () {
        links.classList.remove('open');
        toggle.setAttribute('aria-expanded', 'false');
      });
    });
  }

  var reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- Scroll Reveal Animations ---------- */
  var revealTargets = document.querySelectorAll(
    '.hero-copy, .hero-device, .strip-item, .feature, .doc-block, .cl-entry, .download-inner > *, .compare-table-wrapper, .matcher-container'
  );
  revealTargets.forEach(function (el) { el.classList.add('reveal'); });

  if (reduceMotion || !('IntersectionObserver' in window)) {
    revealTargets.forEach(function (el) { el.classList.add('in'); });
  } else {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('in');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });
    revealTargets.forEach(function (el) { io.observe(el); });

    // Stagger feature cards
    document.querySelectorAll('.feature-grid').forEach(function (grid) {
      Array.from(grid.children).forEach(function (el, i) {
        el.style.transitionDelay = (i % 3) * 80 + 'ms';
      });
    });
  }

  /* ---------- Count-Up Numbers ---------- */
  var counters = document.querySelectorAll('.strip-num[data-count]');
  if (counters.length && !reduceMotion && 'IntersectionObserver' in window) {
    var countIO = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var el = entry.target;
        var target = parseInt(el.getAttribute('data-count'), 10);
        var start = performance.now();
        var dur = 850;
        function tick(now) {
          var p = Math.min((now - start) / dur, 1);
          var eased = 1 - Math.pow(1 - p, 3);
          el.textContent = Math.round(eased * target);
          if (p < 1) requestAnimationFrame(tick);
          else el.textContent = target;
        }
        requestAnimationFrame(tick);
        countIO.unobserve(el);
      });
    }, { threshold: 0.5 });
    counters.forEach(function (el) { countIO.observe(el); });
  }

  /* ---------- Interactive Minecraft Runtime & Renderer Matcher ---------- */
  var verButtons = document.querySelectorAll('.mc-ver-btn');
  var displayTitle = document.getElementById('matcherTitle');
  var displayJava = document.getElementById('matcherJava');
  var displayRenderer = document.getElementById('matcherRenderer');
  var displayReason = document.getElementById('matcherReason');

  var versionData = {
    '1.21.4': {
      title: 'Minecraft 1.21.4 (Tricky Trials / Modern)',
      java: 'Java 21 JRE',
      renderer: 'GL4ES (Default) / Vulkan Zink',
      reason: 'Minecraft 1.20.5+ strictly mandates Java 21+. On Adreno and modern ARM devices, GL4ES offers the best stability, while devices with full Vulkan 1.2 can leverage Zink for peak frame rates.'
    },
    '1.20.4': {
      title: 'Minecraft 1.20.4 (Trails & Tales)',
      java: 'Java 17 JRE',
      renderer: 'GL4ES 1.1.6',
      reason: 'Minecraft 1.17 through 1.20.4 runs natively on Java 17. GL4ES translates OpenGL desktop calls directly to native OpenGL ES with minimal overhead.'
    },
    '1.16.5': {
      title: 'Minecraft 1.16.5 (Nether Update)',
      java: 'Java 8 / Java 11 JRE',
      renderer: 'GL4ES 1.1.6',
      reason: 'Legacy Java 8 / 11 provides optimal compatibility for massive 1.16.5 Forge and Fabric modpacks like RLcraft or Create.'
    },
    '1.12.2': {
      title: 'Minecraft 1.12.2 (Golden Age of Modding)',
      java: 'Java 8 JRE',
      renderer: 'GL4ES 1.1.6',
      reason: 'Classic 1.12.2 modding requires Java 8. Blade Launcher boots isolated Java 8 runtimes with optimized memory arguments for smooth 60 FPS mobile gameplay.'
    },
    '26.2': {
      title: 'Minecraft 26.2+ (Next-Gen Snapshot / Release)',
      java: 'Java 25 JRE',
      renderer: 'Vulkan Zink (Auto-suggested)',
      reason: 'Next-generation Minecraft versions utilize modern Java 25 features and newer LWJGL methods. Blade Launcher automatically enables patched GLFW bridges and Vulkan Zink rendering.'
    }
  };

  verButtons.forEach(function (btn) {
    btn.addEventListener('click', function () {
      verButtons.forEach(function (b) { b.classList.remove('active'); });
      btn.classList.add('active');
      var verKey = btn.getAttribute('data-version');
      var data = versionData[verKey];
      if (data && displayTitle && displayJava && displayRenderer && displayReason) {
        displayTitle.textContent = data.title;
        displayJava.textContent = data.java;
        displayRenderer.textContent = data.renderer;
        displayReason.textContent = data.reason;
      }
    });
  });

  /* ---------- Interactive Device 3D Tilt Effect ---------- */
  var deviceEl = document.querySelector('.hero-device .device');
  if (deviceEl && !reduceMotion) {
    var heroEl = document.querySelector('.hero');
    heroEl.addEventListener('mousemove', function (e) {
      var rect = deviceEl.getBoundingClientRect();
      var x = e.clientX - (rect.left + rect.width / 2);
      var y = e.clientY - (rect.top + rect.height / 2);
      var maxTilt = 12;
      var tiltX = -(y / (window.innerHeight / 2)) * maxTilt;
      var tiltY = (x / (window.innerWidth / 2)) * maxTilt;
      deviceEl.style.transform = 'perspective(1000px) rotateX(' + tiltX.toFixed(2) + 'deg) rotateY(' + tiltY.toFixed(2) + 'deg) translateY(-4px)';
    });

    heroEl.addEventListener('mouseleave', function () {
      deviceEl.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) translateY(0px)';
    });
  }

  /* ---------- Docs Scroll-Spy ---------- */
  var tocLinks = document.querySelectorAll('.docs-toc a[href^="#"]');
  var blocks = document.querySelectorAll('.doc-block[id]');
  if (tocLinks.length && blocks.length && 'IntersectionObserver' in window) {
    var spyIO = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        var link = document.querySelector('.docs-toc a[href="#' + entry.target.id + '"]');
        if (!link) return;
        if (entry.isIntersecting) {
          tocLinks.forEach(function (l) { l.classList.remove('active'); });
          link.classList.add('active');
        }
      });
    }, { rootMargin: '-20% 0px -70% 0px' });
    blocks.forEach(function (b) { spyIO.observe(b); });
  }

});
