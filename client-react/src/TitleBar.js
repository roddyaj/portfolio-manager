function TitleBar(props) {
	const { accounts, selectedAccount, setSelectedAccount, portfolio, requestPortfolio, stopPolling } = props;

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

				<button style={{ marginLeft: 12 }} onClick={() => requestPortfolio(selectedAccount)}><i className="bi bi-arrow-clockwise"></i></button>
				<button style={{ marginLeft: 6 }} onClick={() => stopPolling()}><i className="bi bi-stop-fill"></i></button>

				{
					portfolio && (
						<div>
							<span style={{ marginLeft: 24 }}>Cash: {portfolio.cash.toLocaleString("en-US", currencyOptions)}</span>
							<span style={{ marginLeft: 12 }}>Balance: {portfolio.balance.toLocaleString("en-US", currencyOptions)}</span>
							<span style={{ marginLeft: 12 }}>Return: <span style={{ color: portfolio.portfolioReturn >= 0 ? "green" : "#C00" }}>{(portfolio.portfolioReturn * 100).toFixed(2)}%</span></span>
							<span style={{ marginLeft: 24 }}>{new Date(portfolio.positionsTime).toLocaleString()}</span>
						</div>
					)
				}
			</div>
		</div>
	);
}

export default TitleBar;
