// Generated TypeScript type definitions
// Generated at: 2025-11-25 10:12:32

// Common types
export type ValidationRule = {
  type: string;
  value?: any;
  message: string;
};

export type FieldValidation = {
  name: string;
  rules: ValidationRule[];
  realtime: boolean;
};

export type FormField = {
  name: string;
  type: string;
  label: Record<string, string>;
  placeholder?: Record<string, string>;
  required: boolean;
  readonly: boolean;
  disabled: boolean;
  [key: string]: any;
};

export type ListColumn = {
  name: string;
  label: Record<string, string>;
  type: string;
  sortable: boolean;
  searchable: boolean;
  width: string;
  align: string;
  format?: string;
  [key: string]: any;
};

// users table types
export interface Users {
  id: number;
  name: string;
  email: string;
  age?: number;
  phone?: string;
  created_date: Date | string;
}

export interface UsersForm {
  name: string;
  email: string;
  age?: number;
  phone?: string;
  created_date: Date | string;
}

// categories table types
export interface Categories {
  id: number;
  name: string;
  description?: string;
  sort_order?: number;
}

export interface CategoriesForm {
  name: string;
  description?: string;
  sort_order?: number;
}

// products table types
export interface Products {
  id: number;
  name: string;
  category_id?: number;
  price: number;
  stock?: number;
  is_active: boolean;
}

export interface ProductsForm {
  name: string;
  category_id?: number;
  price: number;
  stock?: number;
  is_active: boolean;
}

// order_details table types
export interface OrderDetails {
  order_id: number;
  product_id: number;
  quantity: number;
  unit_price: number;
}

export interface OrderDetailsForm {
  order_id: number;
  product_id: number;
  quantity: number;
  unit_price: number;
}
