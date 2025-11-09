import React, { useState } from "react";
import { Form } from "react-bootstrap";
import PropTypes from "prop-types";

const SearchableDropdown = ({
  items = [],
  onSelect,
  searchBy = "name",
  onSearchByChange,
  selectedItem = null,
  placeholder = "Search...",
  label = "Select Item",
  required = false,
  searchByOptions = ["name", "email"],
  displayFormat = (item) => `${item.name} - ${item.email}`,
  itemKey = "userId",
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);

  // Filter items based on search query and search type
  const filteredItems = items.filter((item) => {
    if (!searchQuery) return true;

    const query = searchQuery.toLowerCase();
    if (searchBy === "name") {
      return item.name.toLowerCase().includes(query);
    } else if (searchBy === "email") {
      return item.email.toLowerCase().includes(query);
    }
    return true;
  });

  const handleItemSelect = (item) => {
    onSelect(item);
    setSearchQuery("");
    setShowDropdown(false);
  };

  const handleSearchFocus = () => {
    setShowDropdown(true);
  };

  const handleSearchBlur = () => {
    // Delay to allow click on dropdown item
    setTimeout(() => {
      setShowDropdown(false);
    }, 200);
  };

  const handleClear = () => {
    onSelect(null);
    setSearchQuery("");
  };

  return (
    <Form.Group className="mb-3">
      <Form.Label>{label}</Form.Label>

      {/* Toggle between search options */}
      {searchByOptions.length > 1 && (
        <div className="d-flex gap-2 mb-2">
          <div className="btn-group" role="group">
            {searchByOptions.map((option) => (
              <button
                key={option}
                type="button"
                className={`btn btn-sm ${
                  searchBy === option ? "btn-primary" : "btn-outline-primary"
                }`}
                onClick={() => onSearchByChange(option)}
              >
                Search by {option.charAt(0).toUpperCase() + option.slice(1)}
              </button>
            ))}
          </div>
        </div>
      )}

      {/* Searchable Dropdown */}
      <div style={{ position: "relative" }}>
        <Form.Control
          type="text"
          placeholder={selectedItem ? displayFormat(selectedItem) : placeholder}
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onFocus={handleSearchFocus}
          onBlur={handleSearchBlur}
          required={required && !selectedItem}
        />

        {/* Dropdown List */}
        {showDropdown && (
          <div
            style={{
              position: "absolute",
              top: "100%",
              left: 0,
              right: 0,
              maxHeight: "250px",
              overflowY: "auto",
              backgroundColor: "white",
              border: "1px solid #ced4da",
              borderRadius: "0.375rem",
              marginTop: "2px",
              zIndex: 1000,
              boxShadow: "0 4px 6px rgba(0, 0, 0, 0.1)",
            }}
          >
            {filteredItems.length > 0 ? (
              filteredItems.map((item) => (
                <div
                  key={item[itemKey]}
                  onClick={() => handleItemSelect(item)}
                  style={{
                    padding: "10px 15px",
                    cursor: "pointer",
                    borderBottom: "1px solid #f0f0f0",
                    transition: "background-color 0.2s",
                  }}
                  onMouseEnter={(e) =>
                    (e.target.style.backgroundColor = "#f8f9fa")
                  }
                  onMouseLeave={(e) =>
                    (e.target.style.backgroundColor = "white")
                  }
                >
                  <div style={{ fontWeight: 500 }}>{item.name}</div>
                  {item.email && (
                    <div style={{ fontSize: "0.875rem", color: "#6c757d" }}>
                      {item.email}
                    </div>
                  )}
                </div>
              ))
            ) : (
              <div
                style={{
                  padding: "10px 15px",
                  color: "#6c757d",
                  textAlign: "center",
                }}
              >
                No items found matching "{searchQuery}"
              </div>
            )}
          </div>
        )}

        {/* Hidden input to maintain form validation */}
        <input
          type="hidden"
          value={selectedItem ? selectedItem[itemKey] : ""}
          required={required}
        />
      </div>

      {selectedItem && (
        <div className="mt-2">
          <small className="text-muted">
            Selected: {displayFormat(selectedItem)}
            <button
              type="button"
              className="btn btn-link btn-sm text-danger p-0 ms-2"
              onClick={handleClear}
            >
              Clear
            </button>
          </small>
        </div>
      )}
    </Form.Group>
  );
};

SearchableDropdown.propTypes = {
  items: PropTypes.array.isRequired,
  onSelect: PropTypes.func.isRequired,
  searchBy: PropTypes.string,
  onSearchByChange: PropTypes.func,
  selectedItem: PropTypes.object,
  placeholder: PropTypes.string,
  label: PropTypes.string,
  required: PropTypes.bool,
  searchByOptions: PropTypes.arrayOf(PropTypes.string),
  displayFormat: PropTypes.func,
  itemKey: PropTypes.string,
};

export default SearchableDropdown;
