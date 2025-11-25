// Generated React Hooks for table operations
// Generated at: 2025-11-25 13:57:45

import { useState, useEffect } from 'react';

// Common API response type
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

// Common hook for API calls
export const useApi = <T>() => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const callApi = async (url: string, options: RequestInit = {}): Promise<T | null> => {
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
    const data = await callApi<Users[]>(`/api/config/data/users`, {
      method: 'GET',
    }});
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: UsersForm) => {
    const result = await callApi<Users>(`/api/config/data/users`, {
      method: 'POST',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: UsersForm) => {
    const result = await callApi<Users>(`/api/config/data/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>(`/api/config/data/{table_name}/${id}`, {
      method: 'DELETE',
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
    const data = await callApi<Categories[]>(`/api/config/data/categories`, {
      method: 'GET',
    }});
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: CategoriesForm) => {
    const result = await callApi<Categories>(`/api/config/data/categories`, {
      method: 'POST',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: CategoriesForm) => {
    const result = await callApi<Categories>(`/api/config/data/categories/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>(`/api/config/data/{table_name}/${id}`, {
      method: 'DELETE',
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
    const data = await callApi<Products[]>(`/api/config/data/products`, {
      method: 'GET',
    }});
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: ProductsForm) => {
    const result = await callApi<Products>(`/api/config/data/products`, {
      method: 'POST',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: ProductsForm) => {
    const result = await callApi<Products>(`/api/config/data/products/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>(`/api/config/data/{table_name}/${id}`, {
      method: 'DELETE',
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
    const data = await callApi<OrderDetails[]>(`/api/config/data/order_details`, {
      method: 'GET',
    }});
    
    if (data) {
      setRecords(data);
    }
  };

  // Create new record
  const createRecord = async (data: OrderDetailsForm) => {
    const result = await callApi<OrderDetails>(`/api/config/data/order_details`, {
      method: 'POST',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => [...prev, result]);
    }
    
    return result;
  };

  // Update existing record
  const updateRecord = async (id: number, data: OrderDetailsForm) => {
    const result = await callApi<OrderDetails>(`/api/config/data/order_details/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data),
    }});
    
    if (result) {
      setRecords(prev => prev.map(record => 
        record.id === id ? result : record
      ));
    }
    
    return result;
  };

  // Delete record
  const deleteRecord = async (id: number) => {
    const success = await callApi<boolean>(`/api/config/data/{table_name}/${id}`, {
      method: 'DELETE',
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
