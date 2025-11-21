// Generated React Hooks for table operations
// Generated at: 2025-11-18 13:10:16

import { useState } from 'react';
import type { Users, UsersForm, Categories, CategoriesForm, Products, ProductsForm, OrderDetails, OrderDetailsForm } from '../types/generated';

// Common API response type
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

// Common hook for API calls
export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const callApi = async <T>(url: string, options: RequestInit = {}): Promise<T | null> => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...options.headers,
        },
        ...options,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const result: ApiResponse<T> = await response.json();
      
      if (!result.success) {
        throw new Error(result.error || result.message || 'API call failed');
      }

      return result.data || null;
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error';
      setError(errorMessage);
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { callApi, loading, error };
};

// Hook for users table operations
export const useUsers = () => {
  const [records, setRecords] = useState<Users[]>([]);
  const { callApi, loading, error } = useApi();

  // Fetch all records
  const fetchRecords = async () => {
    const data = await callApi<Users[]>('/api/sql/findAll', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'users' }),
    });
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: UsersForm) => {
    const result = await callApi<Users>('/api/sql/create', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'users', data }),
    });
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: UsersForm) => {
    const result = await callApi<Users>('/api/sql/update', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'users', id, data }),
    });
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>('/api/sql/delete', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'users', id }),
    });
    
    if (success) {
      setRecords(prev => prev.filter(record => record.id !== id));
    }
    
    return success;
  };

  return {
    records,
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord,
    loading,
    error,
  };
};

// Hook for categories table operations
export const useCategories = () => {
  const [records, setRecords] = useState<Categories[]>([]);
  const { callApi, loading, error } = useApi();

  // Fetch all records
  const fetchRecords = async () => {
    const data = await callApi<Categories[]>('/api/sql/findAll', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'categories' }),
    });
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: CategoriesForm) => {
    const result = await callApi<Categories>('/api/sql/create', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'categories', data }),
    });
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: CategoriesForm) => {
    const result = await callApi<Categories>('/api/sql/update', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'categories', id, data }),
    });
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>('/api/sql/delete', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'categories', id }),
    });
    
    if (success) {
      setRecords(prev => prev.filter(record => record.id !== id));
    }
    
    return success;
  };

  return {
    records,
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord,
    loading,
    error,
  };
};

// Hook for products table operations
export const useProducts = () => {
  const [records, setRecords] = useState<Products[]>([]);
  const { callApi, loading, error } = useApi();

  // Fetch all records
  const fetchRecords = async () => {
    const data = await callApi<Products[]>('/api/sql/findAll', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'products' }),
    });
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: ProductsForm) => {
    const result = await callApi<Products>('/api/sql/create', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'products', data }),
    });
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: ProductsForm) => {
    const result = await callApi<Products>('/api/sql/update', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'products', id, data }),
    });
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>('/api/sql/delete', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'products', id }),
    });
    
    if (success) {
      setRecords(prev => prev.filter(record => record.id !== id));
    }
    
    return success;
  };

  return {
    records,
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord,
    loading,
    error,
  };
};

// Hook for order_details table operations
export const useOrderDetails = () => {
  const [records, setRecords] = useState<OrderDetails[]>([]);
  const { callApi, loading, error } = useApi();

  // Fetch all records
  const fetchRecords = async () => {
    const data = await callApi<OrderDetails[]>('/api/sql/findAll', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'order_details' }),
    });
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: OrderDetailsForm) => {
    const result = await callApi<OrderDetails>('/api/sql/create', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'order_details', data }),
    });
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record with composite key
  const updateRecord = async (keyValues: { order_id: number; product_id: number }, data: OrderDetailsForm) => {
    const result = await callApi<OrderDetails>('/api/sql/update', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'order_details', keyValues, data }),
    });
    
    if (result) {
      setRecords(prev => prev.map(record => 
        (record.order_id === keyValues.order_id && record.product_id === keyValues.product_id) ? result : record
      ));
    }
    
    return result;
  };

  // Delete record with composite key
  const deleteRecord = async (keyValues: { order_id: number; product_id: number }) => {
    const success = await callApi<boolean>('/api/sql/delete', {
      method: 'POST',
      body: JSON.stringify({ tableName: 'order_details', keyValues }),
    });
    
    if (success) {
      setRecords(prev => prev.filter(record => 
        !(record.order_id === keyValues.order_id && record.product_id === keyValues.product_id)
      ));
    }
    
    return success;
  };

  return {
    records,
    fetchRecords,
    createRecord,
    updateRecord,
    deleteRecord,
    loading,
    error,
  };
};
