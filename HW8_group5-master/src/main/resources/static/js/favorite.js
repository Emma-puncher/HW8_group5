// 收藏管理器
class FavoriteManager {
    constructor() {
        this.storageKey = 'cafe_favorites';
        this.favorites = this.loadFavorites();
        this.listeners = [];
    }

    // 從記憶體載入收藏
    loadFavorites() {
        try {
            const stored = localStorage.getItem(this.storageKey);
            return stored ? JSON.parse(stored) : [];
        } catch (error) {
            console.error('Failed to load favorites:', error);
            return [];
        }
    }

    // 儲存收藏到記憶體
    saveFavorites() {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(this.favorites));
            this.notifyListeners();
        } catch (error) {
            console.error('Failed to save favorites:', error);
            window.utils.showToast('儲存收藏失敗', 'error');
        }
    }

    // 新增監聽器
    addListener(callback) {
        this.listeners.push(callback);
    }

    // 通知所有監聽器
    notifyListeners() {
        this.listeners.forEach(callback => callback(this.favorites));
    }

    // 檢查是否已收藏
    isFavorite(cafeId) {
        return this.favorites.some(fav => fav.id === cafeId);
    }

    // 加入收藏
    addFavorite(cafe) {
        if (this.isFavorite(cafe.id)) {
            window.utils.showToast('此咖啡廳已在收藏清單中', 'warning');
            return false;
        }

        const favoriteItem = {
            id: cafe.id,
            name: cafe.name,
            address: cafe.address,
            features: cafe.features || [],
            hashtags: cafe.hashtags || [],
            score: cafe.score,
            addedAt: new Date().toISOString()
        };

        this.favorites.unshift(favoriteItem);
        this.saveFavorites();
        window.utils.showToast(`已收藏「${cafe.name}」`, 'success');
        return true;
    }

    // 移除收藏
    removeFavorite(cafeId) {
        const cafe = this.favorites.find(fav => fav.id === cafeId);
        this.favorites = this.favorites.filter(fav => fav.id !== cafeId);
        this.saveFavorites();
        
        if (cafe) {
            window.utils.showToast(`已取消收藏「${cafe.name}」`, 'success');
        }
        return true;
    }

    // 切換收藏狀態
    toggleFavorite(cafe) {
        if (this.isFavorite(cafe.id)) {
            return this.removeFavorite(cafe.id);
        } else {
            return this.addFavorite(cafe);
        }
    }

    // 取得所有收藏
    getFavorites() {
        return [...this.favorites];
    }

    // 取得收藏數量
    getCount() {
        return this.favorites.length;
    }

    // 清空所有收藏
    clearAll() {
        if (confirm('確定要清空所有收藏嗎?')) {
            this.favorites = [];
            this.saveFavorites();
            window.utils.showToast('已清空所有收藏', 'success');
            return true;
        }
        return false;
    }

    // 匯出收藏
    exportFavorites() {
        const dataStr = JSON.stringify(this.favorites, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });
        const url = URL.createObjectURL(dataBlob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `cafe_favorites_${new Date().toISOString().split('T')[0]}.json`;
        link.click();
        URL.revokeObjectURL(url);
        window.utils.showToast('收藏清單已匯出', 'success');
    }
}

// 初始化收藏管理器
window.favoriteManager = new FavoriteManager();

// 更新收藏數量顯示
function updateFavoriteCount() {
    const countElement = document.getElementById('favCount');
    if (countElement) {
        const count = window.favoriteManager.getCount();
        countElement.textContent = count;
        countElement.style.display = count > 0 ? 'inline-block' : 'none';
    }
}

// 渲染收藏清單
function renderFavorites() {
    const container = document.getElementById('favoritesContent');
    const favorites = window.favoriteManager.getFavorites();

    if (favorites.length === 0) {
        container.innerHTML = window.utils.createEmptyState(
            'fa-heart-broken',
            '尚無收藏',
            '開始收藏你喜歡的咖啡廳吧!'
        );
        return;
    }

    container.innerHTML = `
        <div class="favorites-actions" style="margin-bottom: 1rem; display: flex; gap: 0.5rem; justify-content: flex-end;">
            <button class="btn-small btn-primary" id="exportFavorites">
                <i class="fas fa-download"></i> 匯出
            </button>
            <button class="btn-small btn-danger" id="clearAllFavorites">
                <i class="fas fa-trash"></i> 清空
            </button>
        </div>
        <div class="results-container">
            ${favorites.map(cafe => window.utils.createCafeCard(cafe, {
                showCheckbox: false,
                showFavorite: true
            })).join('')}
        </div>
    `;

    // 綁定匯出按鈕
    document.getElementById('exportFavorites')?.addEventListener('click', () => {
        window.favoriteManager.exportFavorites();
    });

    // 綁定清空按鈕
    document.getElementById('clearAllFavorites')?.addEventListener('click', () => {
        if (window.favoriteManager.clearAll()) {
            renderFavorites();
            updateFavoriteCount();
        }
    });

    // 綁定收藏按鈕
    attachFavoriteListeners();
}

// 綁定收藏按鈕事件
function attachFavoriteListeners() {
    document.querySelectorAll('.favorite-btn').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            e.stopPropagation();
            const cafeId = btn.dataset.cafeId;
            
            // 從 DOM 中取得咖啡廳資訊
            const card = btn.closest('.cafe-card');
            const cafe = {
                id: cafeId,
                name: card.querySelector('.cafe-title')?.textContent.trim().split('\n')[0] || '',
                address: card.querySelector('.address-link')?.textContent.trim() || '',
                features: Array.from(card.querySelectorAll('.feature-tag')).map(tag => 
                    tag.textContent.trim()
                ),
                hashtags: Array.from(card.querySelectorAll('.hashtag')).map(tag => 
                    tag.textContent.trim().replace('#', '')
                ),
                score: parseFloat(card.querySelector('.cafe-score')?.textContent.trim()) || 0
            };

            window.favoriteManager.toggleFavorite(cafe);
            
            // 更新按鈕狀態
            const isFavorite = window.favoriteManager.isFavorite(cafeId);
            btn.classList.toggle('active', isFavorite);
            btn.setAttribute('aria-label', isFavorite ? '取消收藏' : '加入收藏');
            
            updateFavoriteCount();
            
            // 如果在收藏清單頁面,重新渲染
            const modal = document.getElementById('favoritesModal');
            if (modal.classList.contains('show')) {
                renderFavorites();
            }
        });
    });
}

// 開啟收藏清單
function openFavorites() {
    const modal = document.getElementById('favoritesModal');
    modal.classList.add('show');
    renderFavorites();
}

// 關閉收藏清單
function closeFavorites() {
    const modal = document.getElementById('favoritesModal');
    modal.classList.remove('show');
}

// 初始化收藏功能
function initFavorites() {
    // 更新收藏數量
    updateFavoriteCount();
    
    // 監聽收藏變化
    window.favoriteManager.addListener(() => {
        updateFavoriteCount();
    });

    // 綁定開啟按鈕
    document.getElementById('favoritesBtn')?.addEventListener('click', openFavorites);
    
    // 綁定關閉按鈕
    document.getElementById('closeFavoritesModal')?.addEventListener('click', closeFavorites);
    
    // 點擊背景關閉
    document.getElementById('favoritesModal')?.addEventListener('click', (e) => {
        if (e.target.id === 'favoritesModal') {
            closeFavorites();
        }
    });

    // ESC 鍵關閉
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const modal = document.getElementById('favoritesModal');
            if (modal.classList.contains('show')) {
                closeFavorites();
            }
        }
    });
}

// 匯出函式
window.favoriteModule = {
    init: initFavorites,
    attach: attachFavoriteListeners,
    render: renderFavorites,
    open: openFavorites,
    close: closeFavorites
};