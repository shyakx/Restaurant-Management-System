import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwtToken');
  if (token && !config.url?.includes('/menu/items')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwtToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export interface AuthResponse {
  token: string;
  type: string;
  username: string;
}

export const authService = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    try {
      const response = await api.post('/auth/login', { username, password });
      const { token } = response.data;
      localStorage.setItem('jwtToken', token);
      return response.data;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  },

  logout: () => {
    localStorage.removeItem('jwtToken');
  },

  getToken: () => localStorage.getItem('jwtToken'),

  isAuthenticated: () => !!localStorage.getItem('jwtToken'),
};

export interface MenuItem {
  id?: number;
  name: string;
  description?: string;
  price: number;
  category?: string;
  available: boolean;
}

export interface Category {
  id: string;
  name: string;
  itemCount?: number;
}

export const menuService = {
  // Get all menu items
  getAllMenuItems: async (): Promise<MenuItem[]> => {
    try {
      const response = await api.get('/menu/items');
      return response.data;
    } catch (error) {
      console.error('Error fetching menu items:', error);
      throw error;
    }
  },

  // Get menu items by category
  getMenuItemsByCategory: async (category: string): Promise<MenuItem[]> => {
    try {
      const response = await api.get(`/menu/items?category=${category}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching menu items by category:', error);
      throw error;
    }
  },

  // Get available menu items
  getAvailableMenuItems: async (): Promise<MenuItem[]> => {
    try {
      const response = await api.get('/menu/items?available=true');
      return response.data;
    } catch (error) {
      console.error('Error fetching available menu items:', error);
      throw error;
    }
  },

  // Get all categories
  getCategories: async (): Promise<Category[]> => {
    try {
      const response = await api.get('/menu/categories');
      return response.data;
    } catch (error) {
      console.error('Error fetching categories:', error);
      throw error;
    }
  },

  // Add new menu item
  addMenuItem: async (menuItem: Omit<MenuItem, 'id'>): Promise<MenuItem> => {
    try {
      const response = await api.post('/menu/items', menuItem);
      return response.data;
    } catch (error) {
      console.error('Error adding menu item:', error);
      throw error;
    }
  },

  // Update menu item
  updateMenuItem: async (id: number, menuItem: Partial<MenuItem>): Promise<MenuItem> => {
    try {
      const response = await api.put(`/menu/items/${id}`, menuItem);
      return response.data;
    } catch (error) {
      console.error('Error updating menu item:', error);
      throw error;
    }
  },

  // Delete menu item
  deleteMenuItem: async (id: number): Promise<void> => {
    try {
      await api.delete(`/menu/items/${id}`);
    } catch (error) {
      console.error('Error deleting menu item:', error);
      throw error;
    }
  },
};

export default api;
