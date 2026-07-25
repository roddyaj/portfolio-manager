function TitleBar(props) {
	const { accounts, selectedAccount, setSelectedAccount, portfolio, requestPortfolio, stopPolling, dynamicAllocationPct, onDynamicAllocationChange } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});
	const currencyOptions = { style: "currency", currency: "USD" };

	return (
		<div className="pm-block">
			<div style={{ display: "flex", alignItems: "center" }}>
				<div>
					<select name="accounts" id="accounts" value={selectedAccount != null ? selectedAccount : ""} onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>

				<button className="btn btn-sm btn-primary" style={{ padding: "3px 6px", lineHeight: 1, marginLeft: 12 }} onClick={() => requestPortfolio(selectedAccount, dynamicAllocationPct)}><i className="bi bi-arrow-clockwise"></i></button>
				{/* <button style={{ marginLeft: 6 }} onClick={() => stopPolling()}><i className="bi bi-stop-fill"></i></button> */}

				{
					dynamicAllocationPct !== null && dynamicAllocationPct !== undefined && (
						<div style={{ marginLeft: 24, display: "flex", alignItems: "center" }}>
							<span>Dynamic %:</span>
							<input
								type="number"
								step="0.1"
								value={dynamicAllocationPct}
								onChange={(e) => onDynamicAllocationChange(parseFloat(e.target.value))}
								style={{ width: 70, marginLeft: 6 }}
							/>
						</div>
					)
				}

				{
					portfolio && (
						<div>
							<span style={{ marginLeft: 24 }}>Balance: {portfolio.balance.toLocaleString("en-US", currencyOptions)}</span>
							{/* <span style={{ marginLeft: 12 }}>Return: <span style={{ color: portfolio.portfolioReturn >= 0 ? "green" : "#C00" }}>{(portfolio.portfolioReturn * 100).toFixed(2)}%</span></span> */}
							{/* <span style={{ marginLeft: 12 }}>S&amp;P 500 Return: <span style={{ color: portfolio.sp500YtdReturn >= 0 ? "green" : "#C00" }}>{portfolio.sp500YtdReturn.toFixed(2)}%</span></span> */}
							<span style={{ marginLeft: 24 }}>{new Date(portfolio.positionsTime).toLocaleString()}</span>
						</div>
					)
				}
			</div>
		</div>
	);
}

export default TitleBar;
