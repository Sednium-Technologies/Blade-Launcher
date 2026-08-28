document.documentElement.classList.add('js');

document.addEventListener('DOMContentLoaded', function () {

  /* ---------- mobile nav toggle ---------- */
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

  /* ---------- scroll reveal ---------- */
  var revealTargets = document.querySelectorAll(
    '.hero-copy, .hero-device, .strip > div, .feature, .doc-block, .cl-entry, .download-inner > *'
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
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });
    revealTargets.forEach(function (el) { io.observe(el); });

    // stagger feature cards within their grid
    document.querySelectorAll('.feature-grid').forEach(function (grid) {
      Array.from(grid.children).forEach(function (el, i) {
        el.style.transitionDelay = (i % 3) * 70 + 'ms';
      });
    });
    // stagger changelog items within each entry
    document.querySelectorAll('.cl-body').forEach(function (body) {
      Array.from(body.children).forEach(function (el, i) {
        el.style.setProperty('--d', (i * 55) + 'ms');
        el.classList.add('reveal');
        io.observe(el);
      });
    });
  }

  /* ---------- count-up numbers ---------- */
  var counters = document.querySelectorAll('.strip-num[data-count]');
  if (counters.length && !reduceMotion && 'IntersectionObserver' in window) {
    var countIO = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        var el = entry.target;
        var target = parseInt(el.getAttribute('data-count'), 10);
        var start = performance.now();
        var dur = 700;
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
    }, { threshold: 0.6 });
    counters.forEach(function (el) { countIO.observe(el); });
  }

  /* ---------- docs scroll-spy ---------- */
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
