const FIELD_LABELS = [
  ["id", "ID"],
  ["name", "Имя"],
  ["age", "Возраст"],
  ["city", "Город"],
  ["url", "URL"],
  ["photoUrl", "photoUrl"],
  ["statusText", "Статус"],
  ["lookingFor", "Ищу"],
  ["purpose", "Цель"],
  ["importantInPartner", "Важное в партнёре"],
  ["lifePriorities", "Жизненные приоритеты"],
  ["characterTraits", "Черты характера"],
  ["interestsAndHobbies", "Интересы"],
  ["height", "Рост"],
  ["weight", "Вес"],
  ["bodyType", "Телосложение"],
  ["eyeColor", "Цвет глаз"],
  ["appearance", "Внешность"],
  ["maritalStatus", "Семейное положение"],
  ["relationshipStatus", "Статус отношений"],
  ["children", "Дети"],
  ["education", "Образование"],
  ["occupation", "Профессия"],
  ["activity", "Сфера деятельности"],
  ["housing", "Жильё"],
  ["materialStatus", "Материальное положение"],
  ["materialSupport", "Материальная поддержка"],
  ["smoking", "Курение"],
  ["alcohol", "Алкоголь"],
  ["aboutText", "О себе"],
];

const SHOW_DIVORCED_KEY = "tabor.showDivorced";
const SHOW_WITH_CHILDREN_KEY = "tabor.showWithChildren";
const FAVORITES_MODE_KEY = "tabor.favoritesMode";
const LEGACY_HIDE_DIVORCED_KEY = "tabor.hideDivorced";

const statusEl = document.getElementById("status");
const boardEl = document.getElementById("board");
const showDivorcedEl = document.getElementById("show-divorced");
const showWithChildrenEl = document.getElementById("show-with-children");
const favoritesModeEl = document.getElementById("favorites-mode");

let allProfiles = [];
let hiddenIds = new Set();
let favoriteIds = new Set();
let totalInJson = 0;
let favoritesOnly = localStorage.getItem(FAVORITES_MODE_KEY) === "1";
/** Stack of manually hidden profile ids for Ctrl+Z undo. */
const undoStack = [];

function readShowDivorcedPreference() {
  const stored = localStorage.getItem(SHOW_DIVORCED_KEY);
  if (stored != null) {
    return stored === "1";
  }
  const legacyHide = localStorage.getItem(LEGACY_HIDE_DIVORCED_KEY);
  if (legacyHide != null) {
    return legacyHide !== "1";
  }
  return false;
}

showDivorcedEl.checked = readShowDivorcedPreference();
showDivorcedEl.addEventListener("change", () => {
  localStorage.setItem(SHOW_DIVORCED_KEY, showDivorcedEl.checked ? "1" : "0");
  render();
});

showWithChildrenEl.checked = localStorage.getItem(SHOW_WITH_CHILDREN_KEY) === "1";
showWithChildrenEl.addEventListener("change", () => {
  localStorage.setItem(SHOW_WITH_CHILDREN_KEY, showWithChildrenEl.checked ? "1" : "0");
  render();
});

favoritesModeEl.classList.toggle("active", favoritesOnly);
favoritesModeEl.addEventListener("click", () => {
  favoritesOnly = !favoritesOnly;
  favoritesModeEl.classList.toggle("active", favoritesOnly);
  localStorage.setItem(FAVORITES_MODE_KEY, favoritesOnly ? "1" : "0");
  render();
});

document.addEventListener("keydown", (e) => {
  if (!(e.ctrlKey || e.metaKey) || e.altKey || e.shiftKey) {
    return;
  }
  if (e.code !== "KeyZ") {
    return;
  }
  const tag = document.activeElement && document.activeElement.tagName;
  if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") {
    return;
  }
  e.preventDefault();
  undoLastHide();
});

function isDivorced(profile) {
  return String(profile.maritalStatus || "").trim().toLowerCase() === "разведена";
}

function hasChildren(profile) {
  const value = String(profile.children || "").trim().toLowerCase();
  if (!value || value.startsWith("нет")) {
    return false;
  }
  return true;
}

function photoSrc(profile) {
  if (!profile.id) {
    return null;
  }
  const path = profile.photoPath || "";
  const match = path.match(/\.([a-zA-Z0-9]+)$/);
  const ext = match ? match[1].toLowerCase() : "jpg";
  return `/photos/${encodeURIComponent(profile.id + "." + ext)}`;
}

function openProfile(url) {
  if (!url) {
    return;
  }
  window.open(url, "_blank", "noopener,noreferrer");
}

function createCard(profile) {
  const card = document.createElement("article");
  card.className = "card";
  card.dataset.id = profile.id;
  const profileId = String(profile.id);
  const isFavorite = favoriteIds.has(profileId);

  const src = photoSrc(profile);
  if (src) {
    const img = document.createElement("img");
    img.className = "card-photo" + (profile.url ? " clickable" : "");
    img.alt = profile.name || profile.id || "";
    img.loading = "lazy";
    img.src = src;
    if (profile.url) {
      img.title = "Открыть профиль";
      img.addEventListener("click", () => openProfile(profile.url));
    }
    img.onerror = () => {
      img.remove();
      const placeholder = document.createElement("div");
      placeholder.className = "card-photo missing";
      placeholder.textContent = "Нет фото";
      card.prepend(placeholder);
    };
    card.appendChild(img);
  } else {
    const placeholder = document.createElement("div");
    placeholder.className = "card-photo missing";
    placeholder.textContent = "Нет фото";
    card.appendChild(placeholder);
  }

  const head = document.createElement("div");
  head.className = "card-head";

  const titleWrap = document.createElement("div");
  const title = document.createElement("h2");
  title.textContent = [profile.name, profile.age].filter((v) => v != null && v !== "").join(", ");
  titleWrap.appendChild(title);
  head.appendChild(titleWrap);

  const actions = document.createElement("div");
  actions.className = "card-actions";

  const hideBtn = document.createElement("button");
  hideBtn.type = "button";
  hideBtn.className = "hide-btn";
  hideBtn.textContent = "Скрыть";
  hideBtn.addEventListener("click", () => hideProfile(profile.id, card, hideBtn));
  actions.appendChild(hideBtn);

  const favBtn = document.createElement("button");
  favBtn.type = "button";
  favBtn.className = "fav-btn" + (isFavorite ? " active" : "");
  favBtn.textContent = isFavorite ? "В избранном" : "В избранное";
  favBtn.addEventListener("click", () => toggleFavorite(profileId, favBtn, card));
  actions.appendChild(favBtn);

  head.appendChild(actions);
  card.appendChild(head);

  const fields = document.createElement("dl");
  fields.className = "fields";
  for (const [key, label] of FIELD_LABELS) {
    const value = profile[key];
    if (value == null || value === "" || key === "photoPath" || key === "photoUrl" || key === "url") {
      continue;
    }
    const row = document.createElement("div");
    row.className = "field";
    const dt = document.createElement("dt");
    dt.textContent = label;
    const dd = document.createElement("dd");
    if (key === "url" || key === "photoUrl") {
      const a = document.createElement("a");
      a.href = String(value);
      a.target = "_blank";
      a.rel = "noopener noreferrer";
      a.textContent = String(value);
      dd.appendChild(a);
    } else {
      dd.textContent = String(value);
    }
    row.append(dt, dd);
    fields.appendChild(row);
  }
  card.appendChild(fields);
  return card;
}

function visibleProfiles() {
  return allProfiles
    .filter((p) => {
      if (!p || !p.id || hiddenIds.has(String(p.id))) {
        return false;
      }
      const isFavorite = favoriteIds.has(String(p.id));
      // Обычный борд — без избранного; режим «Избранное» — только избранные.
      if (favoritesOnly ? !isFavorite : isFavorite) {
        return false;
      }
      if (!showDivorcedEl.checked && isDivorced(p)) {
        return false;
      }
      if (!showWithChildrenEl.checked && hasChildren(p)) {
        return false;
      }
      return true;
    })
    .sort((a, b) => {
      const ageA = a.age;
      const ageB = b.age;
      if (ageA == null && ageB == null) {
        return String(a.name || "").localeCompare(String(b.name || ""), "ru");
      }
      if (ageA == null) {
        return 1;
      }
      if (ageB == null) {
        return -1;
      }
      if (ageA !== ageB) {
        return ageA - ageB;
      }
      return String(a.name || "").localeCompare(String(b.name || ""), "ru");
    });
}

function updateStatus(visibleCount) {
  const divorcedHidden = !showDivorcedEl.checked
    ? allProfiles.filter((p) => p && p.id && !hiddenIds.has(String(p.id)) && isDivorced(p)).length
    : 0;
  const withChildrenHidden = !showWithChildrenEl.checked
    ? allProfiles.filter((p) => p && p.id && !hiddenIds.has(String(p.id)) && hasChildren(p)).length
    : 0;

  statusEl.classList.remove("error");
  let text = favoritesOnly ? `Избранное: ${visibleCount}` : `Показано: ${visibleCount}`;
  if (favoriteIds.size) {
    text += ` · в избранном: ${favoriteIds.size}`;
  }
  if (hiddenIds.size) {
    text += ` · скрыто вручную: ${hiddenIds.size}`;
  }
  if (divorcedHidden) {
    text += ` · скрыто «разведена»: ${divorcedHidden}`;
  }
  if (withChildrenHidden) {
    text += ` · скрыто с детьми: ${withChildrenHidden}`;
  }
  text += ` · всего в JSON: ${totalInJson}`;
  statusEl.textContent = text;
}

function render() {
  const visible = visibleProfiles();

  boardEl.replaceChildren();
  if (visible.length === 0) {
    const empty = document.createElement("p");
    empty.className = "empty";
    empty.textContent = favoritesOnly
      ? "В избранном пока никого нет"
      : "Нет профилей для отображения";
    boardEl.appendChild(empty);
  } else {
    for (const profile of visible) {
      boardEl.appendChild(createCard(profile));
    }
  }

  updateStatus(visible.length);
}

function toggleFavorite(profileId, button, card) {
  const adding = !favoriteIds.has(profileId);
  if (adding) {
    favoriteIds.add(profileId);
  } else {
    favoriteIds.delete(profileId);
  }

  button.classList.toggle("active", adding);
  button.textContent = adding ? "В избранном" : "В избранное";

  // Избранное живёт на отдельном борде: с обычного уходит при добавлении,
  // из режима «Избранное» — при снятии.
  const leavesCurrentBoard = (!favoritesOnly && adding) || (favoritesOnly && !adding);
  if (leavesCurrentBoard) {
    card.remove();
    const remaining = boardEl.querySelectorAll(".card").length;
    if (remaining === 0) {
      const empty = document.createElement("p");
      empty.className = "empty";
      empty.textContent = favoritesOnly
        ? "В избранном пока никого нет"
        : "Нет профилей для отображения";
      boardEl.replaceChildren(empty);
    }
    updateStatus(remaining);
  } else {
    updateStatus(boardEl.querySelectorAll(".card").length);
  }

  fetch(adding ? "/api/favorite" : "/api/unfavorite", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id: profileId }),
  }).catch((err) => {
    if (adding) {
      favoriteIds.delete(profileId);
    } else {
      favoriteIds.add(profileId);
    }
    statusEl.textContent = `Ошибка избранного: ${err.message}`;
    statusEl.classList.add("error");
    render();
  });
}

function hideProfile(id, card, button) {
  const profileId = String(id);
  if (hiddenIds.has(profileId)) {
    return;
  }

  hiddenIds.add(profileId);
  undoStack.push(profileId);
  button.disabled = true;
  card.remove();
  const remaining = boardEl.querySelectorAll(".card").length;
  if (remaining === 0) {
    const empty = document.createElement("p");
    empty.className = "empty";
    empty.textContent = favoritesOnly
      ? "В избранном пока никого нет"
      : "Нет профилей для отображения";
    boardEl.replaceChildren(empty);
  }
  updateStatus(remaining);

  fetch("/api/hide", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id: profileId }),
  }).catch((err) => {
    hiddenIds.delete(profileId);
    const idx = undoStack.lastIndexOf(profileId);
    if (idx >= 0) {
      undoStack.splice(idx, 1);
    }
    statusEl.textContent = `Ошибка скрытия: ${err.message}`;
    statusEl.classList.add("error");
    render();
  });
}

function undoLastHide() {
  if (undoStack.length === 0) {
    return;
  }
  const profileId = undoStack.pop();
  if (!hiddenIds.has(profileId)) {
    return;
  }

  hiddenIds.delete(profileId);
  render();

  fetch("/api/unhide", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id: profileId }),
  }).catch((err) => {
    hiddenIds.add(profileId);
    undoStack.push(profileId);
    statusEl.textContent = `Ошибка отмены: ${err.message}`;
    statusEl.classList.add("error");
    render();
  });
}

async function load() {
  try {
    const [profilesRes, hiddenRes, favoritesRes] = await Promise.all([
      fetch("/api/profiles"),
      fetch("/api/hidden"),
      fetch("/api/favorites"),
    ]);
    if (!profilesRes.ok) {
      throw new Error("profiles: HTTP " + profilesRes.status);
    }
    if (!hiddenRes.ok) {
      throw new Error("hidden: HTTP " + hiddenRes.status);
    }
    if (!favoritesRes.ok) {
      throw new Error("favorites: HTTP " + favoritesRes.status);
    }

    const profiles = await profilesRes.json();
    const hidden = await hiddenRes.json();
    const favorites = await favoritesRes.json();
    allProfiles = Array.isArray(profiles) ? profiles : [];
    totalInJson = allProfiles.length;
    hiddenIds = new Set(hidden.hiddenIds || []);
    favoriteIds = new Set(favorites.favoriteIds || []);
    render();
  } catch (err) {
    statusEl.textContent = `Ошибка загрузки: ${err.message}`;
    statusEl.classList.add("error");
  }
}

load();
